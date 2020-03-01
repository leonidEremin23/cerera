/*
 * Copyright (c) 2017. Eremin
 * 28.01.17 18:06
 */

package ae;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by crydb on 28.01.2017.
 * Базовый класс для работы с БД.
 */
public class Database
{
    protected Connection f_connection = null;
    private Statement  f_statement  = null;
    private String  lastError = "";
    
    /**
     * Возвращает соединение с БД
     * @return - соединение с БД
     */
    public synchronized Connection getDbConnection()
    {
        return f_connection;
    }

    /**
     * Возвращает запрос к БД
     * про synchronized - http://java-course.ru/begin/multithread_02/
     * @return запрос
     */
    public synchronized Statement getDbStatement()
    {
        if(f_statement == null) {
            Connection conn = getDbConnection();
            if(conn != null) {
                try {
                    f_statement = conn.createStatement();
                } catch (SQLException e) {
                    lastError = e.getMessage();
                    System.err.println(lastError); // e.printStackTrace();
                }
            }
        }
        return f_statement;
    }

    /**
     * Выполнить оператор SQL
     * @param sql   SQL выражение
     * @return      возвращает кол-во обработанных строк
     */
    public synchronized int ExecSql(String sql)
    {
        int a = 0;
        Statement stm = getDbStatement();
        if(stm != null) {
            try {
                stm.execute(sql);
                a = stm.getUpdateCount();
            } catch (SQLException e) {
                lastError = e.getMessage();
                System.err.println(lastError);  //e.printStackTrace();
            }
        }
        return a;
    }
    
    /**
     *  Возвращает значение первого столбца, первой строки указанного запроса
     *  (аналогично Dlookup в MS Access)
     * @param strSql    строка SQL запроса
     * @return          значение 1 столбца 1-ой строки запроса
     */
    public synchronized String Dlookup(String strSql)
    {
        String result = null;
        ResultSet rst;
        Statement stm = getDbStatement();
        if(stm != null) {
            try {
                rst = stm.executeQuery(strSql);
                if(rst.next()) {
                  result = rst.getString(1);  // взять первый столбец
                }
                rst.close();
            } catch (SQLException e) {
              lastError = e.getMessage();
                System.err.println(lastError); // e.printStackTrace();
            }
        }
        return result;
    }
  
    /**
     * Возвращает колекцию массива строк колонок всех записей запроса, каждый массив
     * представляет собой текстовое значение всех колонок результата запроса
     * @param strSql    запрос SQL
     * @return          список массива строк значений колонок всех записей
     */
    public synchronized ArrayList<String[]> DlookupArray(String strSql)
    {
        ArrayList<String[]>  result = new ArrayList<>();
        Statement stm = getDbStatement();
        if(stm != null) {
            try {
                ResultSet rst = stm.executeQuery(strSql);
                ResultSetMetaData md = rst.getMetaData();
                int Narr = md.getColumnCount();
                while (rst.next()) {
                    String[] row = new String[Narr];
                    for(int i=0; i < Narr; i++) {
                        row[i] = rst.getString(i+1);  // взять i-ый столбец
                    }
                    result.add(row);
                }
                rst.close();
            } catch (SQLException e) {
                lastError = e.getMessage();
                System.err.println(lastError); // e.printStackTrace();
            }
        }
        return result;
    }

    /**
      * Закрыть все ресурсы к БД
      */
    public synchronized void close()
    {
        if(f_statement != null) {
            try {
                f_statement.close();
                f_statement=null;
            } catch (SQLException e) {
                lastError = e.getMessage();
            }
        }
        if(f_connection != null) {
            try {
                f_connection.close();
                f_connection=null;
            } catch (SQLException e) {
                lastError = e.getMessage();
                System.err.println(lastError); // e.printStackTrace();
            }
        }

    }

    public String getLastError() {
      return lastError;
    }

  /**
     * заменяет апостроф на обратный апостров, чтобы можно было вставить в БД
     * и обрамляет строку апострофами, если не null
     * @param str   исходная строка
     * @return      строка с замененными апострофами
     */
    public String s2s(String str)
    {
      if(null == str /*|| str.length()<1*/) {
        return "null";
      }
      String s = str.replace("'", "`");
      return "'" + s + "'";
    }

} // end of class
