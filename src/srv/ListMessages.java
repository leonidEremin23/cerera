/*
 * Copyright (c) 2020. Eremin
 * 03.03.20 15:41
 *
 */

/*
   Получить список сообщений от одного пользователя другому
 */
package srv;

import ae.R;

import java.util.HashMap;

public class ListMessages extends ServerData {
  private final static String sKey = "listmessages";  // ключ метки

  public int[] get(String uFrom, String uTo)
  {
    String pwd = R.getUsrPwd(uTo);
    if(pwd == null) {
      return null;
    }
    HashMap<String,String> args = new HashMap<>();
    if(uFrom != null)
      args.put("from", uFrom);
    if(uTo != null)
      args.put("to", uTo);
    args.put("pwd", pwd);
    //
    int[] ari;
    ari = super.postInt(sKey, args);
    return ari;
  }

} // end of class
