/*
 * Copyright (c) 2020. Eremin
 * 13.03.20 12:24
 *
 */

/*
  Строка данных выводимая в таблицу отправителей сообщений
  отправитель, дата, кол-во сообщений
 */


package senders;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Stroka {

  private StringProperty    from;   // отправитель
  private StringProperty    dat;    // дата последнего сообщения
  private IntegerProperty   nm;     // кол-во сообщений

  public Stroka(String from, String dat, String nm)
  {
    this.from = new SimpleStringProperty(from);
    this.dat  = new SimpleStringProperty(dat);
    this.nm   = new SimpleIntegerProperty(Integer.parseInt(nm));
  }

  public String getFrom() {
    return from.get();
  }

  public StringProperty fromProperty() {
    return from;
  }

  public void setFrom(String from) {
    this.from.set(from);
  }

  public String getDat() {
    return dat.get();
  }

  public StringProperty datProperty() {
    return dat;
  }

  public void setDat(String dat) {
    this.dat.set(dat);
  }

  public int getNm() {
    return nm.get();
  }

  public IntegerProperty nmProperty() {
    return nm;
  }

  public void setNm(int nm) {
    this.nm.set(nm);
  }

  @Override
  public String toString() {
    return
        "" + from.getValue() +
        " (" + dat.getValue() + ")" +
        " / " + nm.getValue()
        ;
  }
} // end of class
