package keygenmy;

import ae.R;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
/*
 Контроллер формирования собственного ключа
 */

public class Controller implements Initializable {

  private Model model = new Model();
  private boolean canRegister;

  @FXML
  Label       lbl_title;
  @FXML
  TextField   txt_user;
  @FXML
  TextField   txt_result;
  @FXML
  TextArea    ta_publicKey;
  @FXML
  TextArea    ta_privateKey;
  @FXML
  Button      btn_keygen;
  @FXML
  Button      btn_check;
  @FXML
  Button      btn_close;
  @FXML
  Label       lbl_server;

  /**
   * Вызывается при инициализации root объекта, будем заполнять ComboBox данными из БД
   * @param location URl
   * @param resources ресурс
   */
  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    // показать имя
    String usr = R.getUsr();
    txt_user.setText(usr);
    // показать сервер
    lbl_server.setText(R.getServer());
    if(usr != null) {
      btn_check.setDisable(true);
      btn_keygen.setDisable(true);
      ta_publicKey.setText(R.getUsrPublickey(usr));
      ta_privateKey.setText(R.getUsrPrivatekey(usr));
      txt_result.setText(R.getUsrPwd(usr));
      lbl_title.setText("данные пользователя");
    }
  }

  /**
   * Обработка нажатия кнопки "сгенерировать ключи"
   * @param ae событие
   */
  public void onclick_btn_keygen(ActionEvent ae)
  {
    if(!canRegister) {
      txt_result.setText("проверьте имя пользователя!");
      return;
    }
    if(model.checkMyKey()) {
      txt_result.setText("Вы уже зарегистрировались! Есть собственный ключ");
      return;
    }
    String[]  keys = model.keyGen();    // генерируем ключи
    ta_publicKey.setText(keys[0]);
    ta_privateKey.setText(keys[1]);
    String usr =  txt_user.getText();   // получим имя пользователя
    String busr = R.trimWS(usr.replace("'", "`"));  // имя без апострофов и пробелов
    boolean res;
    res = model.addUser(busr, keys[0], keys[1], keys[2]);
    if(!res) {
      String errMess = "Ошибка добавления пользователя " + usr + " / " + model.getLastError();
      ta_privateKey.setText(errMess);
    } else {
      txt_result.setText(R.getUsrPwd(busr));
    }
  }

  /**
   * проверить имя пользователя - есть ли оно на сервере
   * @param actionEvent событие
   */
  public void onclick_btn_check(ActionEvent actionEvent)
  {
    canRegister = false;
    String otvet;
    String str = txt_user.getText();  // имя пользователя из поля
    String un = R.trimWS(str);        // уберем пробелы
    if(un == null || un.length() < 1) {
      otvet = "имя не задано!";
    } else {
      txt_user.setText(un);
      boolean check = model.checkUsername(un);
      if (check) {
        otvet = "на сервере существует пользователь " + un;
      } else {
        otvet = "можно регистрировать пользователя " + un;
        canRegister = true; // признак, что можно регистрировать
      }
    }

    txt_result.setText(otvet);
  }

  public void onclick_btn_close(ActionEvent ae)
  {
    closeStage(ae);
  }

  /**
   * Закрыть сцену, а значит и диалоговое окно
   * @param ae  активное событие
   */
  private void closeStage(ActionEvent ae)
  {
    Stage stage = R.event2stage(ae);
    stage.close();
  }

} // end of class

