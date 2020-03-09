/*
 * Copyright (c) 2020. Eremin
 * 09.03.20 18:33
 *
 */
/*
  Строка данных выводимая в таблицу входящих сообщений
 */


package senders;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Stroka {
  private StringProperty  im;   // индекс сообщения
  private StringProperty  from; // отправитель
  private StringProperty  dat;  // дата сообщения

  public Stroka(String im,String from, String dat)
  {
    this.im   = new SimpleStringProperty(im);
    this.from = new SimpleStringProperty(from);
    this.dat  = new SimpleStringProperty(dat);
  }

  public String getIm() {
    return im.get();
  }

  public StringProperty imProperty() {
    return im;
  }

  public void setIm(String im) {
    this.im.set(im);
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

}
