/*
 * Copyright (c) 2020. Eremin
 * 03.03.20 15:41
 *
 */

/*
   Получить список новых сообщений от одного пользователя другому
   результат массив массивов строк
   Collection<String[]>
   ["номер", "отправитель", "получатель", "дата"]

 */
package srv;

import ae.R;

import java.util.List;
import java.util.Map;

public class ListMessages extends ServerData {
  private final static String sKey = "list";  // ключ метки

  /**
   * вернуть список массивов строк с данными о сообщениях
   * https://habr.com/ru/post/237043/
   * @param uFrom отправитель
   * @param uTo   получатель
   * @return список массивов строк
   * [
   *   ["номер", "отправитель", "получатель", "дата"],
   *   [...]
   * ]
   */
  public List<String[]> get(String uFrom, String uTo)
  {
    String pwd = R.getUsrPwd(uTo);
    if(pwd == null) {
      return null;
    }
    Map<String,String> args = prepareArgs(
        "from", uFrom,
        "to",   uTo,
        "pwd",  pwd
    );
    if(args == null)
      return null;
    List<String[]> ars;
    ars = super.postList(sKey, args);
    return ars;
  }

} // end of class
