/*
 * Copyright (c) 2020. Eremin
 * 03.03.20 15:41
 *
 */

/*
   Получить список сообщений от одного пользователя другому
 */
package srv;

import org.json.JSONArray;

import java.util.HashMap;

public class ListMessages extends ServerData {
  private final static String sKey = "list";  // ключ метки

  public int[] get(String uFrom, String uTo)
  {
    HashMap<String,String> args = new HashMap<>();
    if(uFrom != null)
      args.put("from", uFrom);
    if(uTo != null)
      args.put("to", uTo);
    //
    JSONArray ja = super.load(sKey + ".php", sKey, args);
    if(ja != null) {
      int n = ja.length();
      int[] ari = new int[n];
      for(int i=0; i <  n; i++) {
        try {
          ari[i] = (Integer) ja.get(i);
        } catch (Exception e) {
          System.err.println("?-warning-несоответствие типа строки: " + e.getMessage());
        }
      }
      return ari;
    }
    return null;
  }

} // end of class
