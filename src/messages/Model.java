/*
 * Copyright (c) 2020. Eremin
 * 01.03.20 15:27
 *
 */

/*
   Модель отправки и получения сообщений
 */

package messages;

import ae.Database;
import ae.R;
import srv.PubKey;
import srv.UserList;

import java.util.ArrayList;
import java.util.Collection;

public class Model {
  private UserList mUserList = new UserList();  // список пользователей на сервере
  private Database mDb;

  Model()
  {
    mDb = R.getDb();
  }

  /**
   * список пользователей локальной БД
   * @return
   */
  Collection<String> getUserNames()
  {
    ArrayList<String[]> ard;
    ard = mDb.DlookupArray("SELECT usr FROM keys WHERE mykey!=1 ORDER BY usr");
    ArrayList<String> list = new ArrayList<>();
    for(String[] r: ard) list.add(r[0]); // добавим имя в массив
    return  list;
  }

  /**
   * проверить пользователя в локальной БД
   * @param username
   * @return
   */
  boolean checkUserLocal(String username)
  {
    // 1) проверяем в локальной БД
    String un;
    un = mDb.Dlookup("SELECT count(*) FROM keys WHERE usr='" + username + "'");
    if(un != null && Integer.parseInt(un) > 0)
      return true;  // пользователь есть в локальной БД
    return false;
  }

  /**
   * проверить пользователя на сервере и если есть добавить его в локальную БД
   * @param username
   * @return true есть пользователь, false - нет пользователя
   */
  boolean checkUserServer(String username)
  {
    // проверим на сервере
    boolean b = mUserList.inList(username);
    if(b) {
      PubKey pk = new PubKey();
      String pubkey = pk.get(username);
      if(pubkey != null) {
        // вставим в локальную БД
        String sql;
        mDb.ExecSql("DELETE FROM keys WHERE usr='" + username + "'");
        sql = "INSERT INTO keys (usr,publickey) VALUES('" + username + "','" + pubkey + "')";
        int a = mDb.ExecSql(sql);
        if(a != 1) {
          System.err.println("?-error-не добавлен в табл. keys пользователь: " + username);
        } else {
          return true;
        }
      } else {
        System.err.println("?-error-на сервере нет публичного ключа у пользователя: " + username);
      }
    }
    return false;
  }

  /**
   * выдать публичный ключ пользователя из локальной БД
   * @param username имя пользователя
   * @return публичный ключ
   */
  String getPublicKey(String username)
  {
    String pubkey;
    pubkey = mDb.Dlookup("SELECT publickey FROM usr='" + username + "'");
    if(pubkey != null && pubkey.length() < 16)
      return null;  // если ключ короткий - это ошибочный ключ
    return pubkey;
  }

} // end of class

