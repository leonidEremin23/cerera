/*
 * Copyright (c) 2020. Eremin
 * 13.03.20 12:21
 *
 */
/*
   Контролер списка отправителей сообщений и их сообщений
   через web-сервер
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller extends OutputStream implements Initializable {

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

  @FXML
  TextField txt_adresat;

  @FXML
  WebView wv_messages;

  @FXML
  TextField txt_send;

  @FXML
  Button btn_send;

  @FXML
  TextArea  txt_stdout;

  private ObservableList<Stroka> usersData = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // запуск при старте
    initRun();
    beginTimer();
    attachStdout();
  }

  /**
   * Метод вызывается при инициализации контролера
   */
  private void  initRun()
  {
    txt_usr.setText(R.getUsr());
    // создать слушателя события в таблице
    list_senders.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null) {
        filledAdresat(); //
      }
    });
    loadSenders();
    // выбрать нулевой элемент
    Platform.runLater(() -> selectRow(0));
  }

  /**
   * загрузить таблицу отправителей данными
   */
  private void loadSenders()
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
   * зарузить сообщения
   */
  private void  loadMessages()
  {
    String  html = model.loadHtml();
    WebEngine engine = wv_messages.getEngine();
    // engine.load(this.getClass().getResource("../html/test.html").toExternalForm());
    engine.loadContent(html);
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
    javafx.scene.control.MultipleSelectionModel<Stroka> newsel;
    newsel = list_senders.getSelectionModel();
    newsel.select(im);
  }

  /**
   * заполнить имя адресата из ListView
   */
  private void filledAdresat()
  {
    javafx.scene.control.MultipleSelectionModel<Stroka> selmodel = list_senders.getSelectionModel();
    Stroka stro = selmodel.getSelectedItem();
    if(stro != null) {
      String fro = stro.getFrom();  // имя отправителя
      prepareAdresat(fro);
      // обновить список сообщений после отображения
      Platform.runLater(() -> loadMessages());
    }
  }

  /**
   * опрос сервера
   * @param ae событие
   */
  public void onclick_btn_request(ActionEvent ae)
  {
    Model model = new Model();
    int n;
    n = model.loadNewMessages();
    if(n > 0) {
      loadSenders(); //
      // после отображения выбрать 0 строку списка отправителей
      Platform.runLater(() -> selectRow(0));
    }
  }

  /**
   * отправить сообщение адресату
   * @param ae событие
   */
  public void onclick_btn_send(ActionEvent ae)
  {
    String adr = prepareAdresat(txt_adresat.getText());
    if (adr == null || adr.length() < 1) {
      System.out.println(R.Now() + " адресат не указан");
      return;
    }
    // проверим адресата на публичный ключ
    if(model.isPublickey(adr)) {
      // публичный ключ есть, отправляем сообщение
      String msg = txt_send.getText();
      if (msg != null && msg.length() > 0) {
        boolean b;
        b = model.sendMessage(msg);
        if (b) {
          System.out.println(R.Now() + " сообщение отправлено");
          Platform.runLater(() -> afterSendMessage());
        } else {
          System.out.println(R.Now() + " ошибка отправки сообщения");
        }
      } else {
        System.err.println("?-warning-onclick_btn_send() пустая строка сообщения");
      }
    }
  }

  /**
   * после отправки сообщения.
   * очистить поле txt_send и загрузить данные
   */
  private void afterSendMessage()
  {
    txt_send.setText(null);
    loadSenders();
    // обновить список сообщений после отображения
    Platform.runLater(() -> loadMessages());
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

  /**
   * приготовить имя адресата и записать его в поле txt_adresat
   * @param userName новое имя пользователя
   * @return имя адресата
   */
  private String  prepareAdresat(String userName)
  {
    String sadr;
    model.setAdresat(userName);
    sadr = model.getAdresat();
    txt_adresat.setText(sadr);
    return sadr;
  }

  ///////////////////////////////////////////////////////////////////
  // Перенаправление стандартного вывода в TextArea
  // class ... extends OutputStream implements Initializable {
  // стандартный вывод System.output направил в поле txt_out
  // https://code-examples.net/ru/q/19a134d
  private void attachStdout()
  {
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
    System.setOut(new PrintStream(out, true));
  }

  private void appendText(String str) {
    Platform.runLater(() -> txt_stdout.appendText(str));
  }

  @Override
  public void write(int b) {
    Platform.runLater(() -> txt_stdout.appendText(""+b));
  }

} // end of class
