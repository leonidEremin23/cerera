/*
 * Copyright (c) 2020. Eremin
 * 09.03.20 15:45
 *
 */

/*
   Модель списка отправителей сообщений через web-сервер
 */

package messages;

import ae.Database;
import ae.R;
import srv.ListMessages;
import srv.Message;
import srv.PubKey;
import srv.SendMessage;

import java.util.ArrayList;
import java.util.List;

class Model {
  private Database mDb;

  Model()
  {
    this.mDb = R.getDb();
  }

  /**
   * список сообщений для текущего пользователя из локальной БД
   * @return список данных [im, from, dat]
   */
  List<String[]> getMessagesList()
  {
    String  uTo = R.getUsr();
    if(uTo == null)
      return null;
    // загрузим сообщения из локальной БД
    String sql = "SELECT im,ufrom,wdat FROM mess WHERE uto='" + uTo +"' ORDER BY wdat DESC";
    ArrayList<String[]> ars = mDb.DlookupArray(sql);
    return ars;
  }

  /**
   * загрузить новые сообщения для текущего пользователя и их текст в локальную таблицу
   * @return кол-во загруженных сообщений
   */
  int loadNewMessages()
  {
    String  uTo = R.getUsr();
    String  pwd = R.getUsrPwd(uTo);
    if(pwd == null) {
      return 0;
    }
    // получим список новых сообщений и загрузим их в БД
    ListMessages lm = new ListMessages();
    List<String[]> lst = lm.get(null, uTo);
    if(lst != null) {
      for(String[] r: lst) {
        String fmt = "INSERT INTO mess (im,ufrom,uto,wdat) VALUES('%s','%s','%s','%s')";
        String sql = String.format(fmt, r[0],r[1],r[2],r[3]);
        mDb.ExecSql(sql);
      }
    }
    // загрузим текст новых сообщений в БД
    String sql = "SELECT im FROM mess WHERE msg IS NULL AND uto='" + uTo +"'";
    ArrayList<String[]> ars = mDb.DlookupArray(sql);
    int cnt = 0;
    for(String[] r: ars) {
      Integer im = Integer.parseInt(r[0]);
      Message ms = new Message();
      String msg = ms.get(uTo, im);
      if(msg != null) {
        String imsg = mDb.s2s(msg);
        String isql = "UPDATE mess SET msg =" + imsg + " WHERE im=" + im;
        mDb.ExecSql(isql);
        cnt++;
      }
    }
    return cnt;
  }

  boolean sendMessage(String uTo, String textMsg)
  {
    boolean b;
    // проверим публичный ключ, а если нет, то запросим с сервера
    b = isPublickey(uTo);
    if(!b)
      return false;
    SendMessage sm = new SendMessage();
    b = sm.post(uTo, textMsg);
    if(b) {
      // запишем в локальную БД сообщение
      String si = mDb.Dlookup("SELECT MIN(im) FROM mess");
      if(null == si) si ="0";
      int im = Integer.parseInt(si);
      if(im > 0) im = 0;
      im--;
      String sql = "INSERT INTO mess(im,ufrom,uto,msg,wdat) VALUES(" + im + ","
          + "'" + R.getUsr()       + "',"
          + "'" + uTo              + "',"
          +       mDb.s2s(textMsg) + ","
          + "'" + R.Now("yyyy-MM-dd HH:mm:ss") + "')";
      mDb.ExecSql(sql);
    }
    return b;
  }

  /**
   * проверить публичный ключ пользователя в локальной БД,
   * а если его нет, то попытатсья загрузить его из сервера
   * @param usr пользователь
   * @return true есть публичный ключ, fasle нет ключа
   */
  private boolean isPublickey(String usr)
  {

    String pubkey = getFldKeys(usr, "publickey");
    if(pubkey == null || pubkey.length() < 16) {
      PubKey pk = new PubKey();
      String publickey = pk.get(usr);
      if(publickey == null || publickey.length() < 16) {
        System.out.println(R.Now() + " на сервере нет публичного ключа пользователя: " + usr);
        return false;
      }
      mDb.ExecSql("DELETE FROM keys WHERE usr ='" + usr + "'");
      String sql;
      sql = "INSERT INTO keys (usr,publickey) VALUES ('" + usr + "','" + publickey + "')";
      int a;
      a = mDb.ExecSql(sql);
      return (a==1);
    }
    return true;
  }

  /**
   * получить текст сообщения
   * @param im  индекс сообщения
   * @return текст сообщения
   */
  String  getMsg(int im)
  {
    return getFldMess(im, "msg");
  }

  /**
   * получить отправителя сообщения
   * @param im  индекс сообщения
   * @return имя отправителя
   */
  String  getFrom(int im)
  {
    return getFldMess(im, "ufrom");
  }

  /**
   * вернуть значени е поля заданного сообщения
   * @param im      индекс сообщения
   * @param fldName имя поля
   * @return  содержимое поля или '?'
   */
  String getFldMess(int im, String fldName)
  {
    String sql = "SELECT " + fldName + " FROM mess WHERE im=" + im;
    String msg = mDb.Dlookup(sql);
    if(msg == null)
      msg = "?";
    return msg;
  }

  /**
   * вернуть значение поля заданного пользователя
   * @param usr     пользователь
   * @param fldName имя поля
   * @return  содержимое поля или '?'
   */
  String getFldKeys(String usr, String fldName)
  {
    String sql = "SELECT " + fldName + " FROM keys WHERE usr='" + usr + "'";
    String msg = mDb.Dlookup(sql);
    return msg;
  }

} // end of class
