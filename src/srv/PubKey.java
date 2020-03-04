/*
 * Copyright (c) 2020. Eremin
 * 02.03.20 16:53
 *
 */
/*
   Получить публичный ключ пользователя
 */
package srv;

import org.json.JSONArray;

import java.util.HashMap;

public class PubKey extends ServerData {
  private final static String sKey = "pubkey";  // ключ метки

  public String  get(String usr)
  {
    HashMap<String,String> args = new HashMap<>();
    args.put("usr", usr);
    String[] ast = super.postStr(sKey, args);
    if(ast != null) {
      return ast[0];  // публичный ключ пользователя
    }
    return null;
  }

}
