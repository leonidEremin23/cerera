/*
 * Copyright (c) 2020. Eremin
 * 02.03.20 14:10
 *
 */

/*
   Регистрировать нового пользователя
 */
package srv;

import org.json.JSONArray;

import java.util.HashMap;

public class RegisterUser extends ServerData {
  private final static String sUrl = "registeruser.php";  // зарегистрировать нового пользователя
  private final static String sKey = "registeruser";  // ключ метки

  /**
   * послать данные про нового пользователя
   * @param usr     имя пользователея
   * @param pubkey  публичный ключ
   * @return true - пользователь зарегистрирован, false - ошибка регистрации
   */
  public boolean  post(String usr, String pubkey)
  {
    HashMap<String,String> args = new HashMap<>();
    args.put("usr", usr);
    args.put("pubkey", pubkey);
    JSONArray ja = load(sUrl, sKey, args);
    if(ja != null) {
      try {
        String otv = (String) ja.get(0);
        if(otv.contains("true")) {
          return true;
        }
      } catch (Exception e) {
        System.err.println("?-error-нет ответной строки");
      }
    }
    return false;
  }

} // end of class
