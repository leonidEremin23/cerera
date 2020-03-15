/*
 * Copyright (c) 2020. Eremin
 * 09.03.20 18:33
 *
 */
/*
  Строка данных выводимая в список входящих сообщений
  индекс_сообщения, от_кого, дата
 */

package messages;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Stroka {

  private SimpleIntegerProperty im;   // индекс сообщения
  private StringProperty  from; // отправитель
  private StringProperty  msg;  // текст сообщения
  private StringProperty  dat;  // дата сообщения

  public Stroka(String im,String from, String msg, String dat)
  {
    this.im   = new SimpleIntegerProperty(Integer.parseInt(im));
    this.from = new SimpleStringProperty(from);
    this.msg  = new SimpleStringProperty(msg);
    this.dat  = new SimpleStringProperty(dat);
  }

  public int getIm() {
    return im.get();
  }

  public SimpleIntegerProperty imProperty() {
    return im;
  }

  public void setIm(int im) {
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

  public String getMsg() {
    return msg.get();
  }

  public StringProperty msgProperty() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg.set(msg);
  }

  @Override
  public String toString() {
    return "" +
        "" + this.getMsg() +
        "(" + this.getDat() + ")" +
        "[" + this.getIm() + "]" +
        "";
  }

} // end of class
