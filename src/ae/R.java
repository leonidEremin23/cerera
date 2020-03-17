/*
 * Copyright (c) 2020. Eremin
 * 01.03.2020
 *
 */

package ae;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Ресурсный класс
*/
/*
Modify:
  08.03.20  передача и прием сообщений

*/

public class R {
  final static String Ver = "2.0"; // номер версии

  // рабочая БД
  private static String WorkDB = "cerera.db";   // CentOs Linux (в Windows будет D:\var\Gmir\*.db)
  private static Database  db;   // база данных проекта
  // выдать временный каталог (завершается обратным слэшем)
  //public final static String TmpDir = System.getProperty("java.io.tmpdir");
  // разделитель имени каталогов
  //public final static String sep = System.getProperty("file.separator");

  private static String  Usr;        // имя пользователя в программе не задано
  private static String  Server = _r.Server;  // адрес сервера
  static String ProxyServer = _r.proxyserv;  // proxy сервер
  static int    ProxyPort   = _r.proxyport;  // порт proxy-сервера
  static int    TimeOut     = 30000;         // тайм-аут мс
  static String ProxyUser   = _r.proxyuser;
  static String ProxyPass   = _r.proxypass;
  //

  /**
   * Проверить наличие базы данных и создать нужные таблицы
   */
  private static void testDb()
  {
    final String create_tables =
        "CREATE TABLE _Info(key VARCHAR(32) PRIMARY KEY, val TEXT);" +
        "CREATE TABLE keys (usr VARCHAR(32) PRIMARY KEY, mykey INT DEFAULT 0, publickey TEXT, privatekey TEXT, pwd TEXT, wdat DATETIME DEFAULT (DATETIME('now', 'localtime')));" +
        "CREATE TABLE mess (im INT primary key, ufrom VARCHAR(32), uto VARCHAR(32), msg TEXT, datr DATETIME, wdat DATETIME);" +
        // "INSERT INTO _Info(key,val) VALUES('Server','http://localhost/webcerera/srv/');" +
        "";
    if(db == null) {
      db = new DatabaseSqlite(WorkDB);
      //
      String str = db.Dlookup("SELECT COUNT(*) FROM _Info;");
      if (str == null) {
        // ошибка чтения из БД - создадим таблицу
        String[] ssql = create_tables.split(";"); // разобьем на отдельные операторы
        for (String ss: ssql)
          db.ExecSql(ss);
      }
    }
  }

  /**
   * Загрузить параметры по-умолчанию из БД таблицы "_Info"
   */
  static public void loadDefault()
  {
    testDb(); // проверить наличие БД
    // прочитать из БД значения часов выдержки
    R.Server          = R.getInfo(db, "Server",         R.Server);           // сервер проекта
    R.ProxyServer     = R.getInfo(db, "ProxyServer",    R.ProxyServer);      // прокси сервер
    R.ProxyPort       = R.getInfo(db, "ProxyPort",      R.ProxyPort);        // прокси порт
    R.ProxyUser       = R.getInfo(db, "ProxyUser",      R.ProxyUser);        // прокси пользователь
    R.ProxyPass       = R.getInfo(db, "ProxyPass",      R.ProxyPass);        // прокси пароль
    //
  }

  /**
   * Получить имя пользователя. Имя можно получить только
   * из таблицы keys из записи с mykey=1
   * @return имя пользователя
   */
  public static String getUsr()
  {
    if(R.Usr == null) {
      // проверим в табл. keys собственный ключ есть?
      String str = db.Dlookup("SELECT usr FROM keys WHERE mykey=1");
      if (str != null && str.length() > 0) {
        R.Usr = str;
      }
    }
    return R.Usr;
  }

  /**
   * выдать адрес web-сервера
   * @return URL сервера до каталога с "функциями"
   */
  public static String getServer()
  {
    return R.Server;
  }

  /**
   * задать новое имя файла локальной БД
   * @param filename  имя файла
   */
  public static void setWorkDb(String filename)
  {
    dbClose();
    R.WorkDB = filename;
  }

  /**
   * выдать локальную БД проекта
   * @return БД проекта
   */
  public static Database getDb()
  {
    testDb();
    return db;
  }

  static public void dbClose()
  {
    if(db != null) {
      db.close();
      db = null;
    }
  }

  /**
   * Пауза выполнения программы
   * @param time   время задержки, мсек
   */
  static public void sleep(long time)
  {
      try {
          Thread.sleep(time);
      } catch (InterruptedException e) {
          e.printStackTrace();
      }
  }

  /**
   * прочитать ресурсный файл
   * by novel  http://skipy-ru.livejournal.com/5343.html
   * https://docs.oracle.com/javase/tutorial/deployment/webstart/retrievingResources.html
   * @param nameRes - имя ресурсного файла
   * @return -содержимое ресурсного файла
   */
  public static String readRes(String nameRes)
  {
      String str = null;
      ByteArrayOutputStream buf = readResB(nameRes);
      if(buf != null) {
          str = buf.toString();
      }
      return str;
  }

  /**
   * Поместить ресурс в байтовый массив
   * @param nameRes - название ресурса (относительно каталога пакета)
   * @return - байтовый массив
   */
  private static ByteArrayOutputStream readResB(String nameRes)
  {
      try {
          // Get current classloader
          //InputStream is = getClass().getResourceAsStream(nameRes);
          // https://stackoverflow.com/a/8275508
          InputStream is = R.class.getResourceAsStream(nameRes);
          if(is == null) {
              System.out.println("Not found resource: " + nameRes);
              return null;
          }
          // https://habrahabr.ru/company/luxoft/blog/278233/ п.8
          BufferedInputStream bin = new BufferedInputStream(is);
          ByteArrayOutputStream bout = new ByteArrayOutputStream();
          int len;
          byte[] buf = new byte[512];
          while((len=bin.read(buf)) != -1) {
              bout.write(buf,0,len);
          }
          return bout;
      } catch (IOException ex) {
          ex.printStackTrace();
      }
      return null;
  }

  /**
   * Получить из таблицы _Info значение ключа, а если таблицы или ключа нет, то вернуть значение по-умолчанию
   * CREATE TABLE _Info(key text PRIMARY KEY, val text)
   * @param db            база данных с таблицей Info
   * @param keyName       имя ключа
   * @param defaultValue  значение по-умолчанию
   * @return значение ключа
   */
  private static int getInfo(Database db, String keyName, int defaultValue)
  {
      String val = getInfo(db, keyName, Integer.toString(defaultValue));
      return Integer.parseInt(val);
  }

  /**
   * Получить из таблицы _Info значение ключа, а если таблицы или ключа нет, то вернуть значение по-умолчанию
   * CREATE TABLE _Info(key text PRIMARY KEY, val text)
   * @param db            база данных с таблицей Info
   * @param keyName       имя ключа
   * @param defaultValue  значение по-умолчанию
   * @return значение ключа (строка)
   */
  private static String getInfo(Database db, String keyName, String defaultValue)
  {
      String val = db.Dlookup("SELECT val FROM _Info WHERE key='" + keyName + "'");
      if(val == null || val.length() < 1) {
          return defaultValue;
      }
      return val;
  }

  /**
   * Записать в таблицу параметров числовое значение
   * CREATE TABLE _Info(key text PRIMARY KEY, val text)
   * @param db        база данных с таблицей Info
   * @param keyName   имя ключа
   * @param Value     значение
   */
  private static void putInfo(Database db, String keyName, int Value)
  {
    putInfo(db, keyName, Integer.toString(Value));
  }

  /**
   * Записать в таблицу параметров строковое значение
   * CREATE TABLE _Info(key text PRIMARY KEY, val text)
   * @param db        база данных с таблицей Info
   * @param keyName   имя ключа
   * @param Value     значение
   */
  private static void putInfo(Database db, String keyName, String Value)
  {
    String val;
    if(Value == null || Value.length() < 1)
      val = "null";
    else
      val = db.s2s(Value);
    db.ExecSql("UPDATE _Info SET val=" + val + " WHERE key='" + keyName + "'");
  }

//  /**
//   * преобразовать секунды UNIX эпохи в строку даты
//   * @param unix  секунды эпохи UNIX
//   * @return дата и время в формате SQL (ГГГГ-ММ-ДД ЧЧ:ММ:СС)
//   */
//  public static String unix2datetimestr(int unix)
//  {
//    Date date = new Date(unix*1000L);
//    // format of the date
//    SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    //jdf.setTimeZone(TimeZone.getTimeZone("GMT-4"));
//    return jdf.format(date);
//  }

  /*
   * Преобразование строки времени вида ЧЧ:ММ:СС в кол-во секунд
   * @param str   входная строка времени (0:0:2)
   * @return  кол-во секунд

  public static int hms2sec(String str)
  {
    String[] sar;
    int result = 0;
    try {
      sar = str.split(":", 3);
      int ih = Integer.parseInt(sar[0]);
      int im = Integer.parseInt(sar[1]);
      int is = Integer.parseInt(sar[2]);
      result = ih * 3600 + im * 60 + is;
    } catch (Exception e) {
      //e.printStackTrace();
      result = -1;
    }
    return result;
  }
*/

  /////////////////////////////////////////////////////////////////////////////////

  /**
   * вернуть строку без пробелов и апострофов
   * @param str входная строка
   * @return строка без пробелов и апострофов
   */
  public static String  trimWS(String str)
  {
    if(null == str)
      return null;
    String s = str.replaceAll("\\s","");
    String t = s.replaceAll("'","`");
    return t;
  }

  /**
   * Получить из события сцену, где оно случилось
   * @param ae  событие
   * @return  сцена
   */
  public static Stage event2stage(ActionEvent ae)
  {
    Node source = (Node) ae.getSource();
    Stage stage = (Stage) source.getScene().getWindow();
    return stage;
  }

  /**
   * строка текущего времени
   * @return текущее время
   */
  public static String Now()
  {
    return Now("HH:mm:ss"); // yyyy-MM-dd HH:mm:ss
  }

  /**
   * строка текущей даты-времени заданного формата
   * @param format формта
   * @return текущее дата-время
   */
  public static String Now(String format)
  {
    LocalDateTime n = LocalDateTime.now();
    DateTimeFormatter dtpat = DateTimeFormatter.ofPattern(format);
    return n.format(dtpat);
  }

  /**
   * вернуть пароль пользователя
   * @param usr пользователь
   * @return пароль (null ошибка)
   */
  public static String  getUsrPwd(String usr)
  {
    String pwd = R.getDb().Dlookup("SELECT pwd FROM keys where usr='" + usr + "'");
    if(pwd == null || pwd.length() < 1) {
      System.err.println("?-error-getUsrPwd() нет пароля пользователя: " + usr);
      return null;
    }
    return pwd;
  }

  /**
   * получить открытый ключ пользователя
   * @param usr пользователь
   * @return открытый ключ (null ошибка)
   */
  public static String getUsrPublickey(String usr)
  {
    String key = getStrField(usr, "publickey");
    if(key == null)
      return null;
    return key;
  }

  /**
   * получить приватный ключ пользователя
   * @param usr пользователь
   * @return приватный ключ (null ошибка)
   */
  public static String getUsrPrivatekey(String usr)
  {
    String key = getStrField(usr, "privatekey");
    if(key == null)
      return null;
    return key;
  }

  /**
   * вернуть строковое поля из табл. users
   * @param usr   пользователь
   * @param fld   имя поля
   * @return значение поля или null
   */
  private static String getStrField(String usr, String fld)
  {
    String s = getDb().Dlookup("SELECT " + fld + " FROM keys WHERE usr='" + usr + "'");
    if(s == null || s.length() < 1)
      return null;
    return s;
  }

} // end of class
