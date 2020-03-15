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

import java.util.Map;

public class SendMessage extends ServerData {
  private final static String sKey = "send";  // ключ метки

  /**
   * послать новое сообщение, зашифровав его
   * @param usrTo   имя получателя
   * @param msg     текст сообщения
   * @return true - сообщение отправлено, false - ошибка
   */
  public boolean  post(String usrTo, String msg)
  {
    String usrFrom = R.getUsr(); // отправитель
    // зашифруем сообщение
    String pubkey = R.getUsrPublickey(usrTo);
    String pwd = R.getUsrPwd(usrFrom);
    if(pubkey == null || pubkey.length() < 16 || pwd == null) {
      System.err.println("?-error-SendMessage.post() нет публичного ключа получателя");
      return false;
    }
    MyCrypto mc = new MyCrypto(pubkey, null);
    String cry = mc.encryptText(msg);
    //
    Map<String,String> args = prepareArgs(
        "from", usrFrom,
        "pwd",  pwd,
        "to",   usrTo,
        "msg",  cry
    );
    //
    return super.post(sKey, args);
  }

}
