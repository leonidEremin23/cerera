/*
 * Copyright (c) 2020. Eremin
 * 02.03.20 14:10
 *
 */

/*
   Регистрировать нового пользователя
 */
package srv;

import java.util.HashMap;

public class RegisterUser extends ServerData {
  private final static String sKey = "registeruser";  // ключ метки

  /**
   * послать данные про нового пользователя
   * @param usr     имя пользователея
   * @param pubkey  публичный ключ
   * @param pwd     пароль пользователя
   * @return true - пользователь зарегистрирован, false - ошибка регистрации
   */
  public boolean  post(String usr, String pubkey, String pwd)
  {
    HashMap<String,String> args = new HashMap<>();
    args.put("usr", usr);
    args.put("pubkey", pubkey);
    args.put("pwd", pwd);
    boolean b;
    b = super.post(sKey, args);
    return b;
  }

} // end of class
