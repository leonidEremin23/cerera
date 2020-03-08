/*
 * Copyright (c) 2020. Eremin
 * 08.03.20 9:43
 *
 */

/*
   Прочитать заданное сообщение
 */
package srv;

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
    String pwd = getPwd(uTo);
    if(pwd != null) {

      HashMap<String, String> args = new HashMap<>();
      args.put("im",  String.valueOf(im));
      args.put("pwd", pwd);
      //
      String[] ars;
      ars = super.postStr(sKey, args);
      if(ars != null && ars.length >=4) {
        return ars[2]; // текст сообщения
      }
    }
    return null;
  }

} // end of class
