/*
 * Copyright (c) 2020. Eremin
 * 03.03.20 13:27
 *
 */

/*
   Отправить сообщение другому пользователю
 */
package srv;

import ae.Database;
import ae.MyCrypto;
import ae.R;

import java.util.HashMap;

public class SendMessage extends ServerData {
  private final static String sKey = "send";  // ключ метки

  /**
   * послать данные про новое сообщение
   * @param usrTo   имя получателя
   * @param msg     текст сообщения
   * @return true - сообщение отправлено, false - ошибка
   */
  public boolean  post(String usrTo, String msg)
  {
    String usrFrom = R.getUsr(); // отправитель
    Database db = R.getDb();  // база данных
    // зашифруем сообщение
    String pubkey = R.getUsrPublickey(usrTo);
    String pwd = R.getUsrPwd(usrFrom);
    if(pubkey == null || pubkey.length() < 16 || pwd == null) {
      System.err.println("?-error-нет публичного ключа для пользователя: " + usrTo);
      return false;
    }
    MyCrypto mc = new MyCrypto(pubkey, null);
    String cry = mc.encryptText(msg);
    //
    HashMap<String,String> args = new HashMap<>();
    args.put("from", usrFrom);
    args.put("pwd",  pwd);
    args.put("to",   usrTo);
    args.put("msg",  cry);

    return super.post(sKey, args);
  }

}
