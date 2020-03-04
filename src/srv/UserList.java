/*
 * Copyright (c) 2020. Eremin
 * 01.03.20 15:06
 *
 */

/*
   Получить список пользователей из сервера
 */

package srv;

public class UserList extends ServerData {
  private final static String sKey = "userlist";  // ключ метки

  private String[] mUsers = null;

  public String[] read()
  {
    if(mUsers == null) {
      loadUsers();
      if(mUsers == null) {
        mUsers = new String[0];
      }
    }
    return mUsers;
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
    mUsers = super.postStr(sKey, null);
  }

} // end of class
