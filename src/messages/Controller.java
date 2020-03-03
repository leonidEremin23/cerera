/*
 * Copyright (c) 2020. Eremin
 * 01.03.20 15:28
 *
 */

/*
   Контролер отправки и получения сообщений
 */

package messages;

import ae.R;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import srv.ListMessages;
import srv.SendMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;


public class Controller extends OutputStream implements Initializable {

  private Model model = new Model();

  @FXML
  TextField txt_usr;

  @FXML
  TextField txt_to;

  @FXML
  ComboBox<String>  cmb_users;

  @FXML
  TextArea  txt_message;

  @FXML
  Button    btn_test;

  @FXML
  Button    btn_register;


  @FXML
  TextArea  txt_output; // вывод выходного потока стандартный
  @FXML
  TextArea  txt_errout; // вывод выходного потока ошибок

  ///////////////////////////////////////////////////////////////////
  // Перенаправление стандартного вывода в TextArea
  // class ... extends OutputStream implements Initializable {
  // стандартный вывод System.output направил в поле txt_out
  // https://code-examples.net/ru/q/19a134d

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    OutputStream out = new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        appendText(String.valueOf((char) b));
      }
      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        appendText(new String(b, off, len));
      }

      @Override
      public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
      }
    };
    //
    OutputStream err = new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        appendTextErr(String.valueOf((char) b));
      }
      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        appendTextErr(new String(b, off, len));
      }

      @Override
      public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
      }
    };
    System.setOut(new PrintStream(out, true));
    System.setErr(new PrintStream(err, true));
    //
    initialRun();
  }

  @Override
  public void write(int b) throws IOException {
    Platform.runLater(() -> txt_output.appendText(""+b));
  }

  public void appendText(String str) {
    Platform.runLater(() -> txt_output.appendText(str));
  }
  public void appendTextErr(String str) {
    Platform.runLater(() -> txt_errout.appendText(str));
  }

  /**
   * Метод вызывается при инициализации контролера
   */
  private void initialRun()
  {
//    lbl_email.setText(R.Email);
//    //
//    //File file = new File("src/res/app.png");
//    //String uri = file.toURI().toString();
//    //InputStream input2 = getClass().getResourceAsStream("src/res/app1.png");
//    Image image2 = new Image("res/appgray.png");
//    f_image.setImage(image2);
//    //
//    // заполним список пользователей про которых у нас есть ключи
    loadUsers();
    cmb_users.getSelectionModel().select(0);
    onaction_cmb_users(null);
    //
    txt_usr.setText(R.getUsr());
//    //
//    onaction_cmb_users(null); // заполним поле адресата
  }

  /**
   * заполнить список пользователей в комбо-боксе
   */
  private void  loadUsers()
  {
    String  str = cmb_users.getValue();
    Collection<String> users = model.getUserNames();      // заполним список пользователей из локальной БД
    cmb_users.getItems().removeAll(cmb_users.getItems()); // очистить список комбо-бокса
    cmb_users.getItems().addAll(users);
    if(str != null && str.length() > 1)
      cmb_users.getSelectionModel().select(str);  // выбрать ранее выбранный
  }

  public void onclick_btn_send(ActionEvent ae)
  {
    String  un = R.trimWS(txt_to.getText());  // получить имя получателя
    txt_to.setText(un); // запишем на всякий случай, без пробелов
    //
    if(!model.checkUserLocal(un)) {
      if(!model.checkUserServer(un)) {
        System.err.println("?-error-нет пользователя: " + un);
        return;
      } else {
        loadUsers();  // перезагрузить пользователей
      }
    }
    SendMessage sm = new SendMessage();
    boolean b;
    String msg = txt_message.getText();
    b = sm.post(un, msg);
    if(b) {
      System.out.println(R.Now() + " сообщение отправлено");
    }
  }

  public void onclick_btn_receive(ActionEvent ae)
  {
    ListMessages lm = new ListMessages();
    String ufrom = txt_to.getText();
    String uto = txt_usr.getText();
    int[] ims;
    ims = lm.get(ufrom, uto);
    for(int i1: ims) {
      System.out.println(i1);
    }
  }

  /**
   * При изменении списка получателей заполнить поле "получатель"
   * @param ae  событие
   */
  public void onaction_cmb_users(ActionEvent ae)
  {
    String str;
    str = cmb_users.getValue(); // значение выбранноего элемента
    // System.out.println("Акция " + str);
    txt_to.setText(str);
  }

  /**
   * регистрация нового пользователя
   * @param ae
   */
  public void onclick_btn_register(ActionEvent ae)
  {
    keygenmy.Dialog dialog = new keygenmy.Dialog();
    dialog.open(ae);
    //
    txt_usr.setText(R.getUsr());
  }

} // end of class
