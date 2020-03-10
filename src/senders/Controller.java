/*
 * Copyright (c) 2020. Eremin
 * 09.03.20 15:44
 *
 */

/*
   Контролер списка отправителей сообщений через web-сервер
 */

package senders;

import ae.R;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import srv.ListMessages;
import srv.SendMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;


public class Controller implements Initializable {

  private Model model = new Model();

  @FXML
  TextField txt_usr;

  @FXML
  TableView<Stroka> tbl_senders;
  @FXML
  private TableColumn<Stroka, String> col_im;
  @FXML
  private TableColumn<Stroka, String> col_from;
  @FXML
  private TableColumn<Stroka, String> col_dat;

  @FXML
  TextArea  txt_message;

  @FXML
  public TextField txt_from;

  @FXML
  public TextArea txt_receive;

  @FXML
  Button btn_send;

  @FXML
  Button btn_receive;

  @FXML
  Button    btn_register;

  private ObservableList<Stroka> usersData = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    // attachStdout();
    initialRun();
  }

  /**
   * Метод вызывается при инициализации контролера
   */
  private void initialRun()
  {
    // заполним список пользователей про которых у нас есть ключи
//    loadUsers();
//    cmb_users.getSelectionModel().select(0);
    //
    txt_usr.setText(R.getUsr());
  }


  public void onclick_btn_send(ActionEvent ae)
  {
//    String  un = R.trimWS(txt_to.getText());  // получить имя получателя
//    txt_to.setText(un); // запишем на всякий случай, без пробелов
//    //
//    if(!model.checkUserLocal(un)) {
//      if(!model.checkUserServer(un)) {
//        System.err.println("?-error-нет пользователя: " + un);
//        return;
//      } else {
//        loadUsers();  // перезагрузить пользователей
//      }
//    }
//    SendMessage sm = new SendMessage();
//    boolean b;
//    String msg = txt_message.getText();
//    b = sm.post(un, msg);
//    if(b) {
//      System.out.println(R.Now() + " сообщение отправлено");
//    }
  }

  public void onclick_btn_receive(ActionEvent ae)
  {
    //
    loadData();
  }

  /**
   * регистрация нового пользователя
   * @param ae событие
   */
  public void onclick_btn_register(ActionEvent ae)
  {
    keygenmy.Dialog dialog = new keygenmy.Dialog();
    dialog.open(ae);
    //
    txt_usr.setText(R.getUsr());
  }

  /**
   * загрузить таблицу данными
   */
  private void  loadData()
  {
    usersData.clear();  // очистить таблицу от данных
    List<String[]> lst = model.getMessagesList();
    if(lst == null)
      return;
    for(String[] r: lst) {
      usersData.add(new Stroka(r[0],r[1],r[3]));
    }
    //
    col_im.setCellValueFactory(cellData -> cellData.getValue().imProperty());
    col_from.setCellValueFactory(cellData -> cellData.getValue().fromProperty());
    col_dat.setCellValueFactory(cellData -> cellData.getValue().datProperty());
    // заполняем таблицу данными
    tbl_senders.setItems(usersData);
  }

} // end of class
