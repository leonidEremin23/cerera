/*
 * Copyright (c) 2020. Eremin
 * 08.03.20 9:43
 *
 */

/*
   Прочитать заданное сообщение
 */
package srv;

import ae.MyCrypto;
import ae.R;

import java.util.HashMap;

public class Message extends ServerData
{
  private final static String sKey = "message";  // ключ метки

  /**
   * получить сообщение
   * @param uTo имя получателя
   * @param im  номер сообщения
   * @return строка сообщения
   */
  public String get(String uTo, int im)
  {
    String pwd = R.getUsrPwd(uTo);
    if(pwd != null) {
      HashMap<String, String> args = new HashMap<>();
      args.put("im",  String.valueOf(im));
      args.put("pwd", pwd);
      //
      String[] ars;
      ars = super.postStr(sKey, args);
      if(ars != null && ars.length >=4) {
        // расшифруем сообщение
        String msg = ars[2];  // зашифрованное сообщение
        if(msg != null && msg.length() > 1) {
          // есть сообщение, найдем приватный ключ получателя (текущий пользователь)
          String privkey = R.getUsrPrivatekey(uTo);
          if(privkey != null) {
            // есть приватный ключ, расшифруем сообщение
            MyCrypto crypto = new MyCrypto(null, privkey);
            String txt = crypto.decryptText(msg);
            return txt;
          }
        }
      }
    }
    return null;
  }

} // end of class
