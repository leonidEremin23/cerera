package keygenmy;

import ae.Database;
import ae.MyCrypto;
import ae.R;
import srv.RegisterUser;
import srv.UserList;

import java.util.ArrayList;
import java.util.Collection;

/*
 Модель формирования собственного ключа
 */

class Model {

  private Database  mDb;
  private String    public_key, private_key;  // ключи
  private String    lastError;

  private UserList mUserList = new UserList();  // список пользователей на сервере

  Model()
  {
    mDb = R.getDb();
  }

  /**
   * проверить наличие пользователя на сервере
   * @param username имя пользователя
   * @return true - есть такой пользователь,  false - нет пользователя
   */
  boolean checkUsername(String username)
  {
    return mUserList.inList(username);
  }

  /**
   * Генерировать пару ключей RSA
   * @return  String[2] [0]:public [1]:private ключи [2]:случайная строка
   */
  String[] keyGen()
  {
    MyCrypto mc = new MyCrypto(null,null);
    mc.generateKeys();
    String[]  keys = new String[3];
    keys[0] = mc.getPublicKey();
    keys[1] = mc.getPrivateKey();
    keys[2] = mc.randomString(16);
    return keys;
  }

  /**
   * Добавить ключи собственного пользователя в таблицу и сервер
   * @param usrName имя собственного пользователя
   * @param pubKey  публичный ключ
   * @param privKey приватный ключ
   * @param usrPwd  пароль пользователя
   * @return true добавлен пользователь, false не добавлен
   */
  boolean  addUser(String usrName, String pubKey, String privKey,String usrPwd)
  {
    lastError = "Неправильные аргументы";
    if(usrName == null || pubKey == null || privKey == null) return false;
    if(usrName.length()<1 || pubKey.length()<1 || privKey.length()<1) return false;
    if(checkMyKey()) {
      lastError = "Собственный ключ уже есть";
      return false;
    }
    String  sql;
    RegisterUser regusr = new RegisterUser();
    if(regusr.post(usrName, pubKey, usrPwd)) {
      // mykey = 1 - собственный пользователь
      sql = "INSERT INTO keys (mykey,usr,publickey,privatekey,pwd) " +
          "VALUES(1,'" + usrName + "','" + pubKey + "','" + privKey + "','" + usrPwd +"')";
      int a = mDb.ExecSql(sql);
      lastError = mDb.getLastError();
      return  (a == 1);
    }
    return false;
  }

  boolean  delUser(String usrName)
  {
    if(usrName == null || usrName.length()<1) return false;
    String  sql;
    String  un  = mDb.s2s(usrName);
    sql = "DELETE FROM keys WHERE usr=" + un;
    int a = mDb.ExecSql(sql);
    lastError = mDb.getLastError();
    return (a==1);
  }

  String getLastError() {
    return lastError;
  }

  /**
   * Взять список получателей из таблицы keys в виде коллекции
   * @return массив строк
   */
  Collection<String>  getUsrsKeys()
  {
    // получим список имен из БД
    ArrayList<String[]> ardb = mDb.DlookupArray("SELECT usr,mykey FROM keys WHERE (mykey) is null or mykey!=1 ORDER BY mykey,usr");
    ArrayList<String> collect = new ArrayList<>();
    for (String[] rst: ardb) {
      collect.add(rst[0]); // добавим имя в массив
    }
    return  collect;
  }

  /**
   * Проверить наличие в локальной БД собственного ключа
   * @return true есть собственный ключ
   */
  boolean checkMyKey()
  {
    String  str = mDb.Dlookup("SELECT count(*) FROM keys WHERE mykey=1");
    if(Integer.parseInt(str) != 0) {
      return true;
    }
    return false;
  }

} // end of class
