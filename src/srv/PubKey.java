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
  private final static String sUrl = "pubkey.php";  // зарегистрировать нового пользователя
  private final static String sKey = "pubkey";  // ключ метки

  public String  get(String usr)
  {
    HashMap<String,String> args = new HashMap<>();
    args.put("usr", usr);
    JSONArray ja = load(sUrl, sKey, args);
    if(ja != null) {
      try {
        String otv = (String) ja.get(0);
        if(otv.contains("true")) {
          String pubkey;
          pubkey = (String) ja.get(1);
          return pubkey;
        }
      } catch (Exception e) {
        System.err.println("?-error-нет ответной строки");
      }
    }
    return null;
  }

}
