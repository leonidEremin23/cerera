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

import java.util.Map;

public class ServerData {
  private final static String sKeyMetka  = "metka";
  private final static String sPrefixKey = "cerera#";
  private final static String sKeyArray  = "array";

  public JSONArray load(String url, String key)
  {
    return load(url, key, null);
  }

  public JSONArray load(String url, String key, Map<String,String> postArgs)
  {
    ContentHttp conn = new ContentHttp();
    String txt;
    txt = conn.getContent(R.getServer() + url, postArgs);
    if(txt == null) {
      System.err.println("?-error-нет данных от сервера");
      return null;
    }
    try {
      JSONObject jo = new JSONObject(txt);
      // ищем метку
      Object om = jo.get(sKeyMetka);
      if(om == null) {
        System.err.println("?-error-нет метки");
        return null;
      }
      String metka = (String) om;
      if(!metka.contentEquals(sPrefixKey + key)) {
        System.err.println("?-warning-метка: " + metka + " не соответствует, нужно key=" + key);
        return null;
      }
      // ищем массив
      Object oa = jo.get(sKeyArray);
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
    JSONArray ja = load(url, key, args);
    if(ja != null) {
      try {
        String otv = (String) ja.get(0);
        if(otv.contains("true")) {
          return true;
        }
      } catch (Exception e) {
        System.err.println("?-error-нет ответной строки");
      }
    }
    return false;
  }

} // end of class
