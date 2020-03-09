/*
 * Copyright (c) 2020. Eremin
 * 01.03.20 15:27
 *
 */

/*
   Модель отправки и получения сообщений через web-сервер
 */

package messages;

import ae.Database;
import ae.MyCrypto;
import ae.R;
import srv.ListMessages;
import srv.Message;
import srv.PubKey;
import srv.UserList;

import java.util.ArrayList;
import java.util.Collection;

class Model {
  private UserList mUserList = new UserList();  // список пользователей на сервере
  private Database mDb;

  Model()
  {
    this.mDb = R.getDb();
  }

  /**
   * список пользователей локальной БД
   * @return коллекция строк с именами пользователей
   */
  Collection<String> getUserNames()
  {
    ArrayList<String[]> ard;
    ard = mDb.DlookupArray("SELECT usr FROM keys WHERE mykey!=1 ORDER BY usr");
    ArrayList<String> list = new ArrayList<>();
    for(String[] r: ard)
      list.add(r[0]); // добавим имя в массив
    return  list;
  }

  /**
   * проверить пользователя в локальной БД
   * @param username  имя пользователя
   * @return true есть пользователь
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
   * @param username  имя пользователя
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
          System.err.println("?-error-в табл. keys не добавлен пользователь: " + username);
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
   * получить сообщение для текущего от пользователя
   * @param usrFrom имя пользователя
   * @return текст сообщения
   */
  String  getMessage(String usrFrom)
  {
    String usrTo = R.getUsr();
    if(usrTo == null) {
      System.err.println("?-error-не задан текущий пользователь");
      return null;
    }
    // получить список сообщений
    ListMessages lm = new ListMessages();
    int[] nma = lm.getInt(usrFrom, usrTo);
    if(nma != null && nma.length > 0) {
      // список есть
      int im = nma[0];  // номер первого в списке сообщения
      Message ms = new Message();
      String msg = ms.get(usrTo, im);
      return msg;
    }
    return null;
  }

} // end of class
