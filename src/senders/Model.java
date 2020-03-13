/*
 * Copyright (c) 2020. Eremin
 * 13.03.20 12:23
 *
 */

/*
   Модель списка отправителей сообщений через web-сервер
 */

package senders;

import ae.Database;
import ae.R;

import java.util.ArrayList;
import java.util.List;

public class Model {
  private Database mDb;

  Model()
  {
    mDb = R.getDb();
  }

  /**
   * выдать данные по отправителям
   * @return
   */
  List<String[]>  getSenders()
  {
    String  sql;
    sql = "SELECT usr,wdat,1 FROM keys WHERE mykey=0 ORDER BY usr";
    ArrayList<String[]> ar1 = mDb.DlookupArray(sql);
    return ar1;
  }

} // end of class
