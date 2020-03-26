/*
 * Copyright (c) 2020. Eremin
 * 24.03.20 21:46
 *
 */

/*
   Прочитать даты чтения сообщений
 */

package srv;

import ae.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * получить даты чтения сообщений
 */
public class Datr extends ServerData {
  private final static String sKey = "datr";  // ключ метки

  /**
   * выдать список дат чтения сообщений (дата будет в формате SQL YYYY-MM-DD hh:mm:ss)
   * @param imStr  список масивов строк, где первый элемент - номер сообщения
   *               [ ["номер1"], ["номер2"] ]
   * @return массив [ ["номер1","дата_чтения1"], ["номер2","дата_чтения2"] ]
   */
  public List<String[]> get(List<String[]> imStr)
  {
    if(imStr == null || imStr.size() < 1)
      return null;
    //
    final String uTo = R.getUsr();        // пользователь (получатель)
    final String pwd = R.getUsrPwd(uTo);  // пароль пользователя получателя
    // https://docs.oracle.com/javase/8/docs/api/java/util/StringJoiner.html
    StringJoiner  sbuf = new StringJoiner(",", "[", "]");
    for(String[] r: imStr) { sbuf.add(r[0]); }
    //
    Map<String, String> args = prepareArgs(
        "ims", sbuf.toString(),
        "pwd", pwd
    );
    //
    return postList(sKey, args);
  }

//  /**
//   * получить дату чтения сообщения для текущего пользователя
//   * @param imStr строка с номером сообщения
//   * @return дата чтения сообщения
//   */
//  public String get(String imStr)
//  {
//    try {
//      int im = Integer.parseInt(imStr);
//      return get(im);
//    } catch (Exception e) {
//      System.err.println("?-error-Datr.get() ошибка в номере сообщения: " +  imStr + ". " + e.getMessage());
//    }
//    return null;
//  }
//
//  /**
//   * получить дату чтения сообщение для текущего пользователя
//   * @param im  номер сообщения
//   * @return дата чтения сообщения
//   */
//  public String get(int im)
//  {
//    ArrayList<String[]> ari = new ArrayList<>();
//    String[] r = new String[1];
//    r[0] = String.valueOf(im);
//    ari.add(r);
//    List<String[]> ol = get(ari);
//    if(ol != null) {
//      r = ol.get(0);
//      if(r != null && r.length > 1) {
//        return r[1];
//      }
//    }
//    return null;
//  }

} // end of class
