/*
 * Copyright (c) 2020. Eremin
 * 02.03.20 16:33
 *
 */

/*
   Базовый класс по чтению данных с сервера
   данные передаются как JSON объект с полями
    "result" значение - логическое
    "data" значение   - массив данных
 */

package srv;

import ae.ContentHttp;
import ae.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
  boolean post(String key, Map<String,String> args)
  {
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
   * @return массив строк, null - ошибка
   */
  String[] postStr(String key, Map<String,String> args)
  {
    JSONArray ja = load(key, args);
    if(ja != null) {
      ArrayList<String> arr = new ArrayList<>();
      int n = ja.length();
      for(int i = 0; i < n; i++) {
        try {
          Object o = ja.get(i);
          String s = (o.equals(null))? null: (String)o;
          arr.add(s);
        } catch (Exception e) {
          System.err.println("?-error-неправильный тип элемента массива");
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
  int[] postInt(String key, Map<String,String> args)
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
   * Послать запрос к серверу и получить список массивов строк
   * ["номер", "отправитель", "получаталь", "дата"]
   * @param key   ключ операции
   * @param args  аргументы посылки
   * @return true задача выполнена, false задача не выполнена
   */
  List<String[]> postList(String key, Map<String,String> args)
  {
    JSONArray ja = load(key, args);
    if(ja == null)
      return null;
    ArrayList<String[]> alist = new ArrayList<>();
    int n = ja.length();  // кол-во элементов списка
    for(int i = 0; i < n; i++) {
      try {
        JSONArray js = (JSONArray) ja.get(i); // массив строк
        int jn = js.length();
        String[] astr = new String[jn];
        for(int j = 0; j < jn; j++) {
          String s;
          try {
            s = (String) js.get(j);
          } catch (Exception e) {
            s = null;
          }
          astr[j] = s;
        }
        alist.add(astr);
      } catch (Exception e) {
        System.err.println("?-error-postList() ошибка преобразования типа: " + e.getMessage());
      }
    }
    return  alist;
  }

  /**
   * подготовить аргументы запроса из пар строк: имя,значение
   * @param arg  пары: имя,значение
   * @return массив аргументов для запроса
   */
  Map<String,String> prepareArgs(String ... arg)
  {
    int n = arg.length;   // общее кол-во аргументов
    if(n < 2 || (n%2) != 0) {
      System.err.println("?-error-prepareArgs() неверное кол-во аргументов");
      return null;
    }
    Map<String,String> args = new HashMap<>();
    for(int i = 0; i < n; ) {
      String nam = arg[i++];  // имя аргумента
      String val = arg[i++];  // значение параметра
      if(nam != null && val != null) {
        args.put(nam, val);
      }
    }
    return args;
  }

} // end of class
