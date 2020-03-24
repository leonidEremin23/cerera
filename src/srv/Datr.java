/*
 * Copyright (c) 2020. Eremin
 * 24.03.20 21:46
 *
 */

/*
   Прочитать дату чтения сообщения
 */

package srv;

import ae.R;

import java.util.Map;

public class Datr extends ServerData {
  private final static String sKey = "datr";  // ключ метки

  /**
   * получить сообщение для текущего пользователя
   * @param im  номер сообщения
   * @return строка сообщения
   */
  public String get(int im)
  {
    final String uTo = R.getUsr(); // пользователь (получатель)
    final String pwd = R.getUsrPwd(uTo); // пароль пользователя получателя
    Map<String, String> args = prepareArgs(
        "im", String.valueOf(im),
        "pwd", pwd
    );
    String[] ars;
    ars = super.postStr(sKey, args);
    if(ars != null && ars.length > 0) {
      // расшифруем сообщение
      String datr = ars[0];  // дата yyyy-mm-dd hh:mm:ss
      if(datr != null && datr.length() > 9) {
        return datr;
      }
    }
    return null;
  }

} // end of class
