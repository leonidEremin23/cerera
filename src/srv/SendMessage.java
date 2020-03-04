/*
 * Copyright (c) 2020. Eremin
 * 03.03.20 13:27
 *
 */

/*
   Отправить сообщение другому пользователю
 */
package srv;

import ae.MyCrypto;
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
    // зашифруем сообщение
    String pubkey = R.getDb().Dlookup("SELECT publickey FROM keys WHERE usr='" + usr + "'");
    if(pubkey == null || pubkey.length() < 16) {
      System.err.println("?-error-нет публичного ключа для пользователя: " + usr);
      return false;
    }
    MyCrypto mc = new MyCrypto(pubkey, null);
    String cry = mc.encryptText(msg);
    //
    HashMap<String,String> args = new HashMap<>();
    args.put("from", R.getUsr());
    args.put("to",   usr);
    args.put("msg",  cry);

    return super.post(sKey, args);
  }
}
