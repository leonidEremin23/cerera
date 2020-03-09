/*
 * Copyright (c) 2020. Eremin
 * 03.03.20 15:41
 *
 */

/*
   Получить список сообщений от одного пользователя другому
   результат массив массивов строк
   Collection<String[]>
   ["номер", "отправитель", "получатель", "дата"]

 */
package srv;

import ae.R;

import java.util.HashMap;
import java.util.List;

public class ListMessages extends ServerData {
  private final static String sKey = "listmessages";  // ключ метки

  public int[] getInt(String uFrom, String uTo)
  {
    List<String[]> lstr;
    lstr = get(uFrom, uTo);
    if(lstr != null) {
      int n = lstr.size();  // размер списка
      int[] ari = new int[n]; // массив целых
      for(int i = 0; i < n; i++) {
        try {
          String[] sai = lstr.get(i);
          int a = Integer.parseInt(sai[0]);
          ari[i] = a;
        } catch (Exception e) {
          System.err.println("?-error-ListMessages.getInt() ошибка " + e.getMessage());
        }
      }
      return ari;
    }
    return null;
  }

  /**
   * вернуть список массивов строк с данными о сообщениях
   * https://habr.com/ru/post/237043/
   * @param uFrom отправитель
   * @param uTo   получатель
   * @return список массивов строк
   * [ ["номер", "отправитель", "получатель", "дата"], [...] ]
   */
  public List<String[]> get(String uFrom, String uTo)
  {
    HashMap<String,String> args = prepareArgs(uFrom, uTo);
    if(args == null)
      return null;
    List<String[]> ars;
    ars = super.postList(sKey, args);
    return ars;
  }

  /**
   * подготовить аргументы запроса с учетом пароля получателя
   * @param uFrom отправитель
   * @param uTo   получатель
   * @return массив аргументов для запроса
   */
  private HashMap<String,String> prepareArgs(String uFrom, String uTo)
  {
    String pwd = R.getUsrPwd(uTo);
    if(pwd == null) {
      return null;
    }
    HashMap<String,String> args = new HashMap<>();
    if(uFrom != null)
      args.put("from", uFrom);
    if(uTo != null)
      args.put("to", uTo);
    args.put("pwd", pwd);
    //
    return args;
  }


} // end of class
