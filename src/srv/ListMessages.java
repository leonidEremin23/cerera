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
  private final static String sKey = "listmessages";  // ключ метки

  public int[] get(String uFrom, String uTo)
  {
    HashMap<String,String> args = new HashMap<>();
    if(uFrom != null)
      args.put("from", uFrom);
    if(uTo != null)
      args.put("to", uTo);
    //
    int[] ari;
    ari = super.postInt(sKey, args);
    return ari;
  }

} // end of class
