/*
 * Copyright (c) 2020. Eremin
 * 13.03.20 12:23
 *
 */

/*
   Модель списка отправителей и их сообщений
 */

package senders;

import ae.Database;
import ae.R;
import srv.ListMessages;
import srv.Message;
import srv.PubKey;
import srv.SendMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Model {

  private Database mDb;

  private String    mAdresat;   // имя адресата (другого пользователя)

  Model()
  {
    mDb = R.getDb();
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
  private List<String[]>  getMessagesList()
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
   * загрузить новые сообщения для текущего пользователя и их текст
   * в локальную таблицу из web-сервера
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

  /**
   * послать сообщение адресату
   * @param textMsg текст сообщения
   * @return true - сообщение отправлено и записано, false - ошибка отправки
   */
  boolean sendMessage(String textMsg)
  {
    SendMessage sm = new SendMessage();
    boolean b = sm.post(mAdresat, textMsg);
    if(b) {
      // запишем в локальную БД своё сообщение
      // индекс своего сообщения меньше 0.
      String si = mDb.Dlookup("SELECT MIN(im) FROM mess");
      if(null == si) si ="0";
      int im = Integer.parseInt(si);
      if(im > 0) im = 0;
      im--;
      String sql = "INSERT INTO mess(im,ufrom,uto,msg,wdat) VALUES("
                + im               + ","
          + "'" + R.getUsr()       + "',"
          + "'" + mAdresat         + "',"
          +       mDb.s2s(textMsg) + ","
          + "'" + R.Now("yyyy-MM-dd HH:mm:ss")
          + "')";
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
    // https://metanit.com/java/tutorial/7.3.php
    // тело страницы
    StringBuffer body = new StringBuffer();
    List<String[]> lst = getMessagesList(); // список сообщений
    for(String[] r: lst) {
      // 0 - индекс сообщения, 1 - сообщение, 2 - дата
      int im = Integer.parseInt(r[0]); // индекс
      String msg = r[1];  // сообщение
      String dat = formatDate(r[2]);  // дата
      String cls = (im > 0)? "itm": "mym";  // их сообщение : моё сообщение
      final String fmt = "<div class='%s'>%s<br><span class='dt'>%s</span></div>";
      String sdiv = String.format(fmt, cls,msg, dat);
      body.append(sdiv);
    }
    // загрузить шаблон страницы из ресурса
    String txt = R.readRes("/html/mess.html");
    // вставить в шаблон (%s) тело страницы
    String out = String.format(txt, body);
    return out;
  }

  private static final SimpleDateFormat sInpfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static final SimpleDateFormat sOutfmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  /**
   * преобразовать формат даты
   * @param strDat входной формат даты
   * @return выходной формат даты
   */
  private String  formatDate(String strDat)
  {
    try {
      Date dat = sInpfmt.parse(strDat);
      return sOutfmt.format(dat);
    } catch (Exception e) {
      System.err.println("?-error-formatDate() " + e.getMessage());
    }
    return strDat;
  }

} // end of class
