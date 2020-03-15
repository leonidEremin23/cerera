/*
 * Copyright (c) 2020. Eremin
 * 13.03.20 12:23
 *
 */

/*
   Модель списка отправителей сообщений через web-сервер
 */

package senders;

import ae.Database;
import ae.R;
import srv.PubKey;
import srv.SendMessage;

import java.util.ArrayList;
import java.util.List;

public class Model {

  private Database mDb;

  private String    mUsr;       // имя текущего пользователя

  private String    mAdresat;   // имя другого пользователя

  Model()
  {
    mDb = R.getDb();
    mUsr  = R.getUsr();
  }

  /**
   * задать имя другого пользователя (адресат)
   * @param userName новое имя адресата
   */
  void setAdresat(String userName)
  {
    mAdresat = R.trimWS(userName);
  }

  /**
   * выдать имя адресата
   * @return имя адресата
   */
  String  getAdresat()
  {
    return this.mAdresat;
  }

  /**
   * выдать данные по отправителям
   * @return список данных
   */
  List<String[]>  getSenders()
  {
    String  sql;
    sql = "SELECT DISTINCT ufrom,MAX(wdat) as dd,COUNT(*) FROM mess WHERE im > 0 GROUP BY ufrom " +
        "UNION " +
        "SELECT DISTINCT usr,wdat as dd,1 FROM keys WHERE mykey=0 AND usr NOT IN (SELECT ufrom FROM mess) " +
        "ORDER BY dd desc;";
    ArrayList<String[]> ar1 = mDb.DlookupArray(sql);
    return ar1;
  }

  /**
   * выдать список данных по сообщениям
   * индекс > 0 сообщение нам, < 0 наше сообщение
   * @return список данных
   * 0 - индекс сообщения, 1 - сообщение, 2 - дата
   */
  List<String[]>  getMessagesList()
  {
    String  sql;
    sql = "SELECT im,msg,wdat FROM mess WHERE im > 0 AND ufrom='" + mAdresat + "' " +
        "UNION " +
        "SELECT im,msg,wdat FROM mess WHERE im<0 AND uto='" + mAdresat + "' " +
        "ORDER BY wdat;";
    ArrayList<String[]> ar1 = mDb.DlookupArray(sql);
    return ar1;
  }

  /**
   * послать сообщение адресату
   * @param textMsg текст сообщения
   * @return
   */
  boolean sendMessage(String textMsg)
  {
    SendMessage sm = new SendMessage();
    boolean b = sm.post(mAdresat, textMsg);
    if(b) {
      // запишем в локальную БД сообщение
      String si = mDb.Dlookup("SELECT MIN(im) FROM mess");
      if(null == si) si ="0";
      int im = Integer.parseInt(si);
      if(im > 0) im = 0;
      im--;
      String sql = "INSERT INTO mess(im,ufrom,uto,msg,wdat) VALUES(" + im + ","
          + "'" + R.getUsr()       + "',"
          + "'" + mAdresat         + "',"
          +       mDb.s2s(textMsg) + ","
          + "'" + R.Now("yyyy-MM-dd HH:mm:ss") + "')";
      mDb.ExecSql(sql);
    }
    return b;
  }

  /**
   * проверить публичный ключ пользователя в локальной БД,
   * а если его нет, то попытаться загрузить его из сервера
   * @param usr пользователь
   * @return true есть публичный ключ, fasle нет ключа
   */
  boolean isPublickey(String usr)
  {
    String pubkey = getFldKeys(usr, "publickey");
    if(pubkey == null || pubkey.length() < 16) {
      PubKey pk = new PubKey();
      String publickey = pk.get(usr);
      if(publickey == null || publickey.length() < 16) {
        System.out.println(R.Now() + " на сервере нет пользователя: " + usr);
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
   * вернуть значение поля заданного пользователя
   * @param usr     пользователь
   * @param fldName имя поля
   * @return  содержимое поля или '?'
   */
  private String getFldKeys(String usr, String fldName)
  {
    String sql = "SELECT " + fldName + " FROM keys WHERE usr='" + usr + "'";
    String msg = mDb.Dlookup(sql);
    return msg;
  }

  /**
   * формирование html страницы для отображения постов
   * @return строка
   */
  String  loadHtml()
  {
    String html = "";
    html += "<html>";
    html += "<head>" +
        "<style>" +
        ".mymess {" +
        "  color: #3a155d;" +
        "  text-align: right;" +
        "  border: 1px solid #e3d627;" +
        "  padding-right: 16px" +
        "}" +
        ".itmess {" +
        "  color: #0000a5;" +
        "  background-color: #56b3e2;" +
        "  border: 1px solid #3d5d49;" +
        "  padding-left: 5px;" +
        "}" +
        "</style>" +
        " </head>";
    html += "<body onLoad=\"javascript:top.scroll(-1,1000000);\">";
    List<String[]> lst = getMessagesList(); // список сообщений
    for(String[] r: lst) {
      // 0 - индекс сообщения, 1 - сообщение, 2 - дата
      int im = Integer.parseInt(r[0]); // индекс
      String msg = r[1];  // сообщение
      String dat = r[2];  // дата
      String cls = (im> 0)?"itmess": "mymess";  // их сообщение : моё сообщение
      String sdiv = "<div class=\""+ cls + "\">";
      sdiv += msg;
      sdiv += "<br><small>" + dat + "</small>";
      sdiv += "</div>\n";
      html += sdiv;
    }
    html += "</body></html>";
    return html;
  }


} // end of class
