/*
 * Copyright (c) 2020. Eremin
 * 14.03.20 16:47
 *
 */

/*
   Контролер сообщений от/для текущего пользователя и указанного
 */

package messages;

import ae.R;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller extends OutputStream implements Initializable {

  private Model model = new Model();

  @FXML
  TextField txt_usr;

  @FXML
  TextField txt_adresat;

  @FXML
  Button btn_close;

  @FXML
  Button btn_request;

  @FXML
  WebView wv_messages;

  @FXML
  TextArea  txt_send;

  @FXML
  Button btn_send;

  @FXML
  TextArea  txt_stdout;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    initialRun();
    attachStdout();
  }

  /**
   * Метод вызывается при инициализации контролера
   */
  private void initialRun()
  {
    //
    txt_usr.setText(R.getUsr());

    //
//    // создать слушателя события в таблице
//    list_senders.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
//      if (newSelection != null) {
//        // onclick_btn_message(null);
//      }
//    });
    //
    // загрузить данные о сообщениях
    loadData();
    //
    // TODO
    beginTimer(); // запустить таймер опроса сервера
    //
  }

  /**
   * загрузить таблицу данными
   */
  private void  loadData()
  {
//    usersData.clear();  // очистить таблицу от данных
//    List<String[]> lst = model.getMessagesList();
//    if(lst == null)
//      return;
//    for(String[] r: lst) {
//      usersData.add(new Stroka(r[0], r[1], r[2], r[3]));
//    }
    //
//    col_im.setCellValueFactory(cellData -> cellData.getValue().imProperty());
//    col_from.setCellValueFactory(cellData -> cellData.getValue().fromProperty());
//    col_dat.setCellValueFactory(cellData -> cellData.getValue().datProperty());
    //
    // https://progi.pro/stroki-cveta-javafx-listview-5904063
    // заполняем таблицу данными
    //list_senders.setItems(usersData);
    String  html = model.loadHtml();
    WebEngine engine = wv_messages.getEngine();
    // engine.load(this.getClass().getResource("../html/test.html").toExternalForm());
    engine.loadContent(html);
    //
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
    //
    System.setOut(new PrintStream(out, true));
  }

  private void appendText(String str) {
    Platform.runLater(() -> txt_stdout.appendText(str));
  }

  @Override
  public void write(int b) throws IOException {
    //Platform.runLater(() -> txt_stdout.appendText(""+b));
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

  void setAdresat(String userName)
  {
    model.setAdresat(userName);
    txt_adresat.setText(model.getAdresat());
    loadData();
  }

  /**
   * опрос сервера
   * @param ae событие
   */
  public void onclick_btn_request(ActionEvent ae) {
    messages1.Model model = new messages1.Model();
    int n;
    n = model.loadNewMessages();
    if (n > 0) {
//      TableView.TableViewSelectionModel<Stroka> selectionModel = tbl_senders.getSelectionModel();
//      Stroka stro = selectionModel.getSelectedItem();
//      int im = 0;
//      if (stro != null) im = stro.getIm();
      //
      loadData();
      // выбрать после отображения
      //Platform.runLater(() -> selectRow(0));
    }
  }

  public void onclick_btn_send(ActionEvent ae)
  {
    String uTo = txt_adresat.getText();
    model.setAdresat(uTo);
    if (uTo == null || uTo.length() < 1) {
      System.out.println(R.Now() + " адресат не указан");
      return;
    }
    txt_adresat.setText(model.getAdresat());
    //
    String msg = txt_send.getText();
    boolean b;
    b = model.sendMessage(msg);
    if(b)
      System.out.println(R.Now() + " сообщение отправлено");
    else
      System.err.println(R.Now() + " ошибка отправки сообщения");
    // txt_send.setText(null);
    Platform.runLater(() -> afterSendMessage());
  }

  private void afterSendMessage()
  {
    txt_send.setText(null);
    loadData();
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


} // end of class
