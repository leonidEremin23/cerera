/*
 * Copyright (c) 2020. Eremin
 * 01.03.20 15:28
 *
 */

/*
   Контролер отправки и получения сообщений
 */

package messages;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller extends OutputStream implements Initializable {

  private Model model = new Model();

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
//    loadUsers();
//    cmb_users.getSelectionModel().select(0);
//    //
//    onaction_cmb_users(null); // заполним поле адресата
  }


  public void onclick_btn_test(ActionEvent ae)
  {
    String[] astr;
    srv.UserList usrs = new srv.UserList();
    astr = usrs.read();
    System.err.println(astr.length);
    for(String s: astr) {
      System.out.println(s);
    }

  }


  /**
   * регистрация нового пользователя
   * @param ae
   */
  public void onclick_btn_register(ActionEvent ae)
  {
    keygenmy.Dialog dialog = new keygenmy.Dialog();
    dialog.open(ae);
  }

} // end of class
