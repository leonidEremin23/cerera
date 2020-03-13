/*
 * Copyright (c) 2020. Eremin
 * 13.03.20 12:21
 *
 */
/*
   Контролер списка отправителей сообщений через web-сервер
 */

package senders;

import ae.R;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

  private Model model = new Model();

  @FXML
  TextField  txt_usr;

  @FXML
  Button btn_close;

  @FXML
  Button btn_register;

  @FXML
  Button btn_request;

  @FXML
  ListView<Stroka>  list_senders;

  private ObservableList<Stroka> usersData = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // запуск при старте
    initRun();
  }

  /**
   * Метод вызывается при инициализации контролера
   */
  private void  initRun()
  {
    txt_usr.setText(R.getUsr());
    loadData();

  }

  /**
   * загрузить таблицу данными
   */
  private void  loadData()
  {
    usersData.clear();  // очистить таблицу от данных
    List<String[]> lst = model.getSenders();
    if(lst == null)
      return;
    for(String[] r: lst) {
      usersData.add(new Stroka(r[0],r[1],r[2]));
    }
    //
//    col_im.setCellValueFactory(cellData -> cellData.getValue().imProperty());
//    col_from.setCellValueFactory(cellData -> cellData.getValue().fromProperty());
//    col_dat.setCellValueFactory(cellData -> cellData.getValue().datProperty());
    // заполняем таблицу данными
    list_senders.setItems(usersData);
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

  public void onclick_btn_close(ActionEvent ae)
  {
    // закрыть окно
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

  /**
   * выбрать строку таблицы с указанным индексом сообщения,
   * если указан 0, то выделение на самой первой строке
   * @param im индекс сообщения
   */
  private void selectRow(int im)
  {
//    MultiplySelectionModel<Stroka> newselectionModel = list_senders.getSelectionModel();
//    ObservableList<Stroka> odat = list_senders.getItems();
//    int n = odat.size();
//    for(int i = 0; i < n; i++) {
//      messages.Stroka stro = odat.get(i);
//      int imi = stro.getIm();
//      if(imi == im || im == 0) {
//        newselectionModel.select(stro);
//        break;
//      }
//    }
  }

  /**
   * опрос сервера
   * @param ae событие
   */
  public void onclick_btn_request(ActionEvent ae)
  {
    messages.Model model2 = new messages.Model();
    int n;
    n = model2.loadNewMessages();
    if(n > 0) {
//      TableView.TableViewSelectionModel<Stroka> selectionModel = tbl_senders.getSelectionModel();
//      Stroka stro = selectionModel.getSelectedItem();
//      int im = 0;
//      if (stro != null) im = stro.getIm();
      //
      loadData();
      // выбрать после отображения
      Platform.runLater(() -> selectRow(0));
    }
  }

  private int count = 0;
  // запуск таймера для обновления списка сообщений
  private void beginTimer()
  {
    Timeline timeline = new Timeline();
    timeline.setCycleCount(Animation.INDEFINITE);
    KeyFrame keyFrame = new KeyFrame(
        Duration.seconds(10),
        event -> {
          // https://issue.life/questions/53587355
          //txt_send.setText(String.valueOf(count++));
          count++;
          // обновить данные с сервера
          onclick_btn_request(null);
        }
    );
    timeline.getKeyFrames().add(keyFrame);
    System.out.println("TimeLine thread id "+ Thread.currentThread().getId());
    timeline.play();
  }


} // end of class
