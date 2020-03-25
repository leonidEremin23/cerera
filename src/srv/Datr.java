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

import java.util.List;
import java.util.Map;

public class Datr extends ServerData {
  private final static String sKey = "datr";  // ключ метки

  /**
   * получить дату чтения сообщения для текущего пользователя
   * @param imStr строка с номером сообщения
   * @return дата чтения сообщения
   */
  public String get(String imStr)
  {
    try {
      int im = Integer.parseInt(imStr);
      return get(im);
    } catch (Exception e) {
      System.err.println("?-error-Datr.get() ошибка в номере сообщения: " +  imStr + ". " + e.getMessage());
    }
    return null;
  }

  /**
   * получить дату чтения сообщение для текущего пользователя
   * @param im  номер сообщения
   * @return дата чтения сообщения
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

  /**
   * выдать список дат чтения сообщений
   * @param imsStr список масивов строк, где первый элемент - номер сообщения
   *               [ ["номер1"], ["номер2"] ]
   * @return массив [ ["номер1","дата_чтения2"], ["номер2","дата_чтения2"] ]
   */
  public List<String[]> get(List<String[]> imsStr)
  {
    if(imsStr == null || imsStr.size() < 1)
      return null;
    //
    final String uTo = R.getUsr();        // пользователь (получатель)
    final String pwd = R.getUsrPwd(uTo);  // пароль пользователя получателя
    //
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("[");
    String sep = "";
    for(String[] r: imsStr) {
      sbuf.append(sep).append(r[0]);
      sep = ",";
    }
    sbuf.append("]");
    Map<String, String> args = prepareArgs(
        "ims", sbuf.toString(),
        "pwd", pwd
    );
    List<String[]> ars = postList(sKey, args);
    return ars;
  }


} // end of class
