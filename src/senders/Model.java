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
import srv.*;

import java.text.SimpleDateFormat;
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
    return mDb.DlookupArray(sql);
  }

  /**
   * выдать список данных по сообщениям.
   * индекс 0 сообщение нам, 1 наше сообщение.
   * у чужих сообщений дата чтения всегда NULL
   * @return список данных
   * 0 - индекс сообщения, 1 - сообщение, 2 - дата, 3 - дата чтения
   */
  private List<String[]>  getMessagesList()
  {
    String  sql;
    sql = "SELECT 0,msg,wdat,NULL FROM mess WHERE ufrom='" + mAdresat + "' " +
          "UNION " +
          "SELECT 1,msg,wdat,datr FROM mess WHERE uto='" + mAdresat + "' " +
          "ORDER BY wdat;";
    return mDb.DlookupArray(sql);
  }

  /**
   * загрузить новые сообщения для текущего пользователя и их текст
   * в локальную таблицу из web-сервера. А потом, проверить даты чтения
   * собственных сообщений и заполнить их в локальной БД.
   * @return кол-во загруженных сообщений
   */
  int loadNewMessages()
  {
    final String  uMe = R.getUsr();       // имя пользователя
    final String  pwd = R.getUsrPwd(uMe); // пароль пользователя
    if(pwd == null) {
      return 0;
    }
    //
    purgeDb();
    // получим список новых сообщений и загрузим их в локальную БД
    ListMessages lm = new ListMessages();
    List<String[]> lst = lm.get(null, uMe, pwd);
    if(lst != null) {
      for(String[] r: lst) {
        String fmt = "INSERT INTO mess (im,ufrom,uto,wdat) VALUES('%s','%s','%s','%s')";
        String sql = String.format(fmt, r[0],r[1],r[2],r[3]);
        mDb.ExecSql(sql);
      }
    }
    //
    int cnt = 0;
    // загрузим текст новых сообщений в БД
    List<String[]> arnm = mDb.DlookupArray(
        "SELECT im FROM mess WHERE msg IS NULL AND uto='" + uMe +"'"
    );
    if(arnm != null && arnm.size() > 0) {
      // есть новые сообщения с незаполненным текстом
      Message ms = new Message();
      for (String[] r: arnm) {
        // индекс сообщения
        int im = R.intval(r[0]);
        // получить текст сообщения
        String msg = ms.get(im);
        if(msg != null) {
          String txt = mDb.s2s(msg);
          mDb.ExecSql("UPDATE mess SET msg =" + txt + " WHERE im=" + im);
          cnt++;
        }
      }
    }
    // загрузим даты прочтения наших сообщений, которые еще не прочитаны получателями
    // получить список наших непрочитанных сообщений
    List<String[]> ardr = mDb.DlookupArray(
        "SELECT im FROM mess WHERE datr IS NULL AND ufrom='" + uMe +"'"
    );
    if(ardr != null && ardr.size() > 0) {
      // список не пустой
      // преобразовать список массивов строк с номерами в массив чисел
      // http://habr.com/ru/company/luxoft/blog/270383/
      // http://annimon.com/article/2778
      int[] ims = ardr.stream().mapToInt(s->Integer.parseInt(s[0])).toArray();
      Datr dr = new Datr();
      List<String[]> arsim = dr.get(ims);
      if(arsim != null) {
        for (String[] r: arsim) {
          // дата прочтения сообщения
          if(r[1] != null) {
            int     im = R.intval(r[0]);
            String sda = R.s2s(r[1]);
            mDb.ExecSql("UPDATE mess SET datr=" + sda + " WHERE im=" + im);
            cnt++;  // как-бы загрузили
          }
        }
      }
    }
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
   * вернуть значение поля у заданного пользователя
   * @param usr     пользователь
   * @param fldName имя поля
   * @return  содержимое поля или '?'
   */
  private String getFldKeys(String usr, String fldName)
  {
    String sql = "SELECT " + fldName + " FROM keys WHERE usr='" + usr + "'";
    return mDb.Dlookup(sql);
  }

  /**
   * формирование html страницы для отображения постов
   * @return строка
   */
  String  loadHtml()
  {
    final String fmt = "<div class='%s%s'>%s<br><span class='dt'>%s</span></div>";
    // https://metanit.com/java/tutorial/7.3.php
    // тело страницы
    StringBuffer body = new StringBuffer();
    List<String[]> lst = getMessagesList(); // список сообщений
    for(String[] r: lst) {
      // 0 - признак (0 чужое, 1 свое), 1 - сообщение, 2 - дата, 3 - дата чтения
      // класс стиля блока сообщения на основе индекса-признака
      String cls = "sm" + r[0];  // их сообщение sm0, моё сообщение sm1;
      // класс прочитанного (своё) сообений
      String clr = (r[3] == null)? "": " r";
      String msg = r[1];  // сообщение
      String dat = formatDate(r[2]);  // дата
      // замена угловых скобок
      String str = msg.replace("<", "&lt;").replace(">", "&gt;");
      String sdv = String.format(fmt, cls, clr, str, dat);
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
