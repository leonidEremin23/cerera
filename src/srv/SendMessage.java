/*
 * Copyright (c) 2020. Eremin
 * 03.03.20 13:27
 *
 */

/*
   Отправить сообщение другому пользователю
 */
package srv;

import ae.R;

import java.util.HashMap;

public class SendMessage extends ServerData {
  private final static String sKey = "send";  // ключ метки

  /**
   * послать данные про новое сообщение
   * @param usr     имя получателя
   * @param msg     текст сообщения
   * @return true - пользователь зарегистрирован, false - ошибка регистрации
   */
  public boolean  post(String usr, String msg)
  {
    HashMap<String,String> args = new HashMap<>();
    args.put("from", R.getUsr());
    args.put("to", usr);
    args.put("msg", msg);
    boolean b;
    b = super.post(sKey, args);
    return b;
  }
}
