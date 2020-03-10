/*
 * Copyright (c) 2020. Eremin
 * 02.03.20 16:53
 *
 */
/*
   Получить публичный ключ пользователя
 */
package srv;

import java.util.Map;

public class PubKey extends ServerData {
  private final static String sKey = "pubkey";  // ключ метки

  public String  get(String usr)
  {
    Map<String,String> args = prepareArgs("usr", usr);
    String[] ast = super.postStr(sKey, args);
    if(ast != null) {
      return ast[0];  // публичный ключ пользователя
    }
    return null;
  }

}
