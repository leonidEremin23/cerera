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
import java.util.Map;

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
    Map<String,String> args = prepareArgs(
        "usr",    usr,
        "pubkey", pubkey,
        "pwd",    pwd
    );
    boolean b;
    b = super.post(sKey, args);
    return b;
  }

} // end of class
