/*
 * Copyright (c) 2020. Eremin
 * 02.03.20 16:33
 *
 */

/*
   Базовый класс по чтению данных с сервера
   данные передаются как JSON объект с полями
    "metka" значение - строка "cerera#имяключа"
    "array" значение - массив данных
 */

package srv;

import ae.ContentHttp;
import ae.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class ServerData {
  private final static String sKeyResult = "result";
  private final static String sKeyData   = "data";

  /**
   * запрос к серверу и анализ ответа
   * @param key       ключ запроса
   * @param postArgs  аргументы POST
   * @return массив JSON
   */
  JSONArray load(String key, Map<String,String> postArgs)
  {
    ContentHttp conn = new ContentHttp();
    String txt;
    txt = conn.getContent(R.getServer() + key + ".php", postArgs);
    if(txt == null) {
      System.err.println("?-error-нет данных от сервера");
      return null;
    }
    try {
      JSONObject jo = new JSONObject(txt);
      // ищем поле result(boolean)
      Object or = jo.get(sKeyResult);
      if(or == null) {
        System.err.println("?-error-нет результата");
        return null;
      }
      boolean r = (boolean) or;
      if(!r)
        return null;  // результат не true
      // ищем массив
      Object oa = jo.get(sKeyData);
      if(oa == null) {
        System.err.println("?-error-нет массива");
        return null;
      }
      JSONArray ja = (JSONArray) oa;
      return ja;
    } catch (Exception e) {
      System.err.println("?-error-" + e.getMessage());
    }
    return null;
  }

  /**
   * Послать запрос к серверу и проанализировать ответ на запрошенную операцию
   * @param key   ключ операции
   * @param args  аргументы посылки
   * @return true задача выполнена, false задача не выполнена
   */
  boolean post(String key, Map<String, String> args)
  {
    String url = key + ".php";
    JSONArray ja = load(key, args);
    if(ja != null) {
      return true;
    }
    return false;
  }

  /**
   * Послать запрос к серверу и получить массив строк
   * @param key   ключ операции
   * @param args  аргументы посылки
   * @return true задача выполнена, false задача не выполнена
   */
  String[] postStr(String key, Map<String, String> args)
  {
    JSONArray ja = load(key, args);
    if(ja != null) {
      ArrayList<String> arr = new ArrayList<>();
      int n = ja.length();
      for(int i = 0; i < n; i++) {
        try {
          String s = (String) ja.get(i);
          arr.add(s);
        } catch (Exception e) {
          System.err.println("?-error-тип элемента массива не String");
          return null;
        }
      }
      // ArrayList в массив
      // https://stackoverflow.com/questions/4042434/converting-arrayliststring-to-string-in-java
      String[] as = arr.toArray(new String[0]);
      return  as;
    }
    return null;
  }

  /**
   * Послать запрос к серверу и получить массив чисел
   * @param key   ключ операции
   * @param args  аргументы посылки
   * @return true задача выполнена, false задача не выполнена
   */
  int[] postInt(String key, Map<String, String> args)
  {
    JSONArray ja = load(key, args);
    if(ja != null) {
      int n = ja.length();
      int[] ar = new int[n];
      for(int i = 0; i < n; i++) {
        try {
          Integer ii = (Integer) ja.get(i);
          ar[i] = ii;
        } catch (Exception e) {
          System.err.println("?-error-тип элемента массива не int");
          return null;
        }
      }
      return  ar;
    }
    return null;
  }

  /**
   * вернуть пароль пользователя
   * @param usr пользователь
   * @return пароль (null ошибка)
   */
  public String  getPwd(String usr)
  {
    String pwd = R.getDb().Dlookup("SELECT pwd FROM keys where usr='" + usr + "'");
    if(pwd == null || pwd.length() < 1) {
      System.err.println("?-error-getPwd() нет пароля пользователя: " + usr);
      return null;
    }
    return pwd;
  }

  /**
   * получить открытый ключ пользователя
   * @param usr пользователь
   * @return открытый ключ (null ошибка)
   */
  public String getPublickey(String usr)
  {
    String key = getStrField(usr, "publickey");
    if(key == null) {
      System.err.println("?-error-getPublickey() нет открытого ключа пользователя: " + usr);
      return null;
    }
    return key;
  }

  /**
   * получить приватный ключ пользователя
   * @param usr пользователь
   * @return приватный ключ (null ошибка)
   */
  public String getPrivatekey(String usr)
  {
    String key = getStrField(usr, "privatekey");
    if(key == null) {
      System.err.println("?-error-getPublickey() нет приватного ключа пользователя: " + usr);
      return null;
    }
    return key;
  }

  /**
   * вернуть строковое поля из табл. users
   * @param usr   пользователь
   * @param fld   имя поля
   * @return значение поля или null
   */
  private String getStrField(String usr, String fld)
  {
    String s = R.getDb().Dlookup("SELECT " + fld + " FROM keys WHERE usr='" + usr + "'");
    if(s == null || s.length() < 1) {
      return null;
    }
    return s;
  }

} // end of class
