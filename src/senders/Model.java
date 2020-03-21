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

  private static final int  sAgoDays = 180;  // время жизни сообщений (дни)

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
    sql =
        "SELECT DISTINCT ufrom,MAX(wdat) as dd,COUNT(*) FROM mess WHERE ufrom!='" +R.getUsr() + "' GROUP BY ufrom " +
        "UNION " +
        "SELECT DISTINCT usr,wdat as dd,0 FROM keys WHERE mykey=0 AND usr NOT IN (SELECT ufrom FROM mess) " +
        "ORDER BY dd desc;";
    ArrayList<String[]> ar1 = mDb.DlookupArray(sql);
    return ar1;
  }

  /**
   * выдать список данных по сообщениям.
   * индекс 0 сообщение нам, 1 наше сообщение
   * @return список данных
   * 0 - индекс сообщения, 1 - сообщение, 2 - дата
   */
  private List<String[]>  getMessagesList()
  {
    String  sql;
    sql = "SELECT 0,msg,wdat,im FROM mess WHERE ufrom='" + mAdresat + "' " +
          "UNION " +
          "SELECT 1,msg,wdat,im FROM mess WHERE uto='" + mAdresat + "' " +
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
    final String  uTo = R.getUsr();       // имя пользователя
    final String  pwd = R.getUsrPwd(uTo); // пароль пользователя
    if(pwd == null) {
      return 0;
    }
    //
    purgeDb();
    // получим список новых сообщений и загрузим их в локальную БД
    ListMessages lm = new ListMessages();
    List<String[]> lst = lm.get(null, uTo, pwd);
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
      int im;
      try {
        im = Integer.parseInt(r[0]);
      } catch (Exception e) {
        System.err.println("?-error-loadNewMessages() неверный номер сообщения " + r[0] + ". " + e.getMessage());
        continue;
      }
      // получить текст сообщения
      Message ms = new Message();
      String msg = ms.get(im);
      if(msg != null) {
        String imsg = mDb.s2s(msg);
        String isql = "UPDATE mess SET msg =" + imsg + " WHERE im=" + im;
        mDb.ExecSql(isql);
        cnt++;
      }
    }
    //
    // purgeDb();
    //
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
    int im = sm.post(mAdresat, textMsg);
    if(im > 0) {
      // запишем в локальную БД своё сообщение
      String sql = "INSERT INTO mess(im,ufrom,uto,msg,wdat) VALUES("
                + im               + ","
          + "'" + R.getUsr()       + "',"
          + "'" + mAdresat         + "',"
          +       mDb.s2s(textMsg) + ","
          + "'" + R.Now("yyyy-MM-dd HH:mm:ss")
          + "')";
      mDb.ExecSql(sql);
      return true;
    }
    return false;
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
    final String fmt = "<div class='%s'>%s<br><span class='dt'>%s</span></div>";
    // https://metanit.com/java/tutorial/7.3.php
    // тело страницы
    StringBuffer body = new StringBuffer();
    List<String[]> lst = getMessagesList(); // список сообщений
    for(String[] r: lst) {
      // 0 - индекс сообщения (0 чужое, 1 свое), 1 - сообщение, 2 - дата
      // класс стиля блока сообщения на основе индекса-признака
      String cls = "sm" + r[0];  // их сообщение sm0, моё сообщение sm1;
      String msg = r[1];  // сообщение
      String dat = formatDate(r[2]);  // дата
      // замена угловых скобок
      String str = msg.replace("<", "&lt;").replace(">", "&gt;");
      String sdv = String.format(fmt, cls, str, dat);
      body.append(sdv);
    }
    // загрузить шаблон страницы из ресурса
    String txt = R.readRes("/html/mess.html");
    // вставить в шаблон (@@@) тело страницы
    String out = txt.replace("@@@", body);
    return out;
  }

  private static final SimpleDateFormat  sInpfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static final SimpleDateFormat  sOutfmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
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

  /**
   * очистить локальную БД от старых сообщений, время
   * которых более sAgoDays дней назад
   */
  private void purgeDb()
  {
//    final DateTimeFormatter sDtmfmt = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
//    LocalDateTime dat = LocalDateTime.now().minusHours(sTTL);
//    String str = sDtmfmt.format(dat);
//    String sql = "DELETE FROM mess WHERE wdat < '" + str + "'";
    int a;
    String sql;
    sql = "DELETE FROM mess WHERE wdat < " +
          "DATETIME('now','localtime','-" + sAgoDays + " days')";
    a = mDb.ExecSql(sql);
    if(a > 0) {
      System.err.println("?-warning-удалено старых сообщений: " + a);
    }
  }

} // end of class
