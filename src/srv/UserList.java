/*
 * Copyright (c) 2020. Eremin
 * 01.03.20 15:06
 *
 */

/*
   Получить список пользователей из сервера
 */

package srv;

import org.json.JSONArray;
import java.util.ArrayList;

public class UserList extends ServerData {
  private final static String sUrl = "userlist.php";  // получить список пользователей
  private final static String sKey = "userlist";  // ключ метки

  private ArrayList<String> mUsers = null;

  public String[] read()
  {
    if(mUsers == null) {
      loadUsers();
    }
    // https://stackoverflow.com/questions/4042434/converting-arrayliststring-to-string-in-java
    String[] astr = mUsers.toArray(new String[0]);
    return astr;
  }

  /**
   * очистить кэш данных
   */
  public void clean()
  {
    mUsers = null;
  }

  /**
   * проверяет наличие в списке указанного имени
   * @param username  имя
   * @return true - пользоатель есть в списке, false - нет в списке
   */
  public boolean  inList(String username)
  {
    read();
    for (String u: mUsers) {
      if(u.contains(username)) {
        return true;
      }
    }
    return false;
  }

  /**
   * загрузить список пользователей
   */
  private void  loadUsers()
  {
    ArrayList<String> arr = new ArrayList<>();
    JSONArray ja = super.load(sUrl, sKey);
    if(ja != null) {
      int n = ja.length();
      for(int i=0; i <  n; i++) {
        try {
          String str = (String) ja.get(i);
          arr.add(str);
        } catch (Exception e) {
          System.err.println("?-warning-несоответствие типа строки: " + e.getMessage());
        }
      }
    }
    mUsers = arr;
  }

} // end of class
