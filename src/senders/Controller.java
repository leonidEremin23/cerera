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
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.net.URL;
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
  public TextArea txt_send;

  @FXML
  Button  btn_request;

  @FXML
  Button btn_message;

  @FXML
  Button btn_send;

  @FXML
  Button    btn_register;

  private ObservableList<Stroka> usersData = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // attachStdout();
    initialRun();
    beginTimer();
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
    //
    // создать слушателя событие в таблице
    tbl_senders.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null) {
        onclick_btn_message(null);
      }
    });
    //
    // загрузить данные о сообщениях
    loadData();
  }


  public void onclick_btn_send(ActionEvent ae)
  {
    String uTo = txt_from.getText();
    String msg = txt_send.getText();
    boolean b;
    b = model.sendMessage(uTo, msg);
    String otv;
    if(b)
      System.out.println(R.Now() + " сообщение отправлено");
    else
      System.err.println(R.Now() + " ошибка отправки сообщения");
  }


  public void onclick_btn_message(ActionEvent ae)
  {
    TableView.TableViewSelectionModel<Stroka> selectionModel = tbl_senders.getSelectionModel();
    Stroka stro = selectionModel.getSelectedItem();
    if(stro != null) {
      int im = Integer.parseInt(stro.getIm());
      // System.out.println("test " + mind);
      String msg = model.getMsg(im);
      String ufr = model.getFrom(im);
      txt_message.setText(msg);
      txt_from.setText(ufr);
    }
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
   * опрос сервера
   * @param ae событие
   */
  public void onclick_btn_request(ActionEvent ae)
  {
    int n;
    n = model.loadNewMessages();
    if(n > 0) {
      TableView.TableViewSelectionModel<Stroka> selectionModel = tbl_senders.getSelectionModel();
      Stroka stro = selectionModel.getSelectedItem();
      String im = stro.getIm();

      loadData();
      // выбрать после отображения
      Platform.runLater(() -> selectRow(im));
    }
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
      usersData.add(new Stroka(r[0],r[1],r[2]));
    }
    //
    col_im.setCellValueFactory(cellData -> cellData.getValue().imProperty());
    col_from.setCellValueFactory(cellData -> cellData.getValue().fromProperty());
    col_dat.setCellValueFactory(cellData -> cellData.getValue().datProperty());
    // заполняем таблицу данными
    tbl_senders.setItems(usersData);
  }

  /**
   * выбрать строку с указанной строкой
   * @param im
   */
  void selectRow(String im)
  {
    TableView.TableViewSelectionModel<Stroka> newselectionModel = tbl_senders.getSelectionModel();
    ObservableList<Stroka> odat = tbl_senders.getItems();
    int n = odat.size();
    for(int i = 0; i < n; i++) {
      Stroka stro = odat.get(i);
      String imi = stro.getIm();
      if(imi.contentEquals(im)) {
        newselectionModel.select(stro);
      }
    }
//    getModelItem(i)
//    newselectionModel.select(stro);
  }

  protected int count = 0;
  // запуск таймера
  private void beginTimer()
  {
    Timeline timeline = new Timeline();
    timeline.setCycleCount(Animation.INDEFINITE);
    KeyFrame keyFrame = new KeyFrame(
        Duration.seconds(5),
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
