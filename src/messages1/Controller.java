/*
 * Copyright (c) 2020. Eremin
 * 09.03.20 15:44
 *
 */

/*
   Контролер сообщений через web-сервер
 */

package messages1;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class Controller extends OutputStream implements Initializable {

  private Model model = new Model();

  @FXML
  TextField txt_usr;

  @FXML
  TableView<Stroka> tbl_senders;
  @FXML
  private TableColumn<Stroka, Number> col_im; // вместо Integer надо использовать Number!
  // https://stackoverflow.com/a/56656665

  @FXML
  private TableColumn<Stroka, String> col_from;
  @FXML
  private TableColumn<Stroka, String> col_dat;

  @FXML
  TextArea  txt_message;

  @FXML
  public TextField txt_adresat;

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

  @FXML
  TextArea  txt_stdout;

  private ObservableList<Stroka> usersData = FXCollections.observableArrayList();

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
    // создать слушателя события в таблице
    tbl_senders.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null) {
        onclick_btn_message(null);
      }
    });
    //
    // загрузить данные о сообщениях
    loadData();
    //
    beginTimer(); // запустить таймер опроса сервера
    //
  }

  /**
   * установить имя адресата
   * https://askdev.ru/q/peredacha-parametrov-javafx-fxml-12180/
   * @param usr имя адресата
   */
  public void setAdresat(String usr)
  {
    txt_adresat.setText(usr);
  }

  /**
   * отправить сообщение адресату
   * @param ae событие
   */
  public void onclick_btn_send(ActionEvent ae)
  {
    String uTo = txt_adresat.getText();
    if (uTo == null || uTo.length() < 1) {
      System.out.println(R.Now() + " адресат не указан");
      return;
    }
    uTo = R.trimWS(uTo);  // удалить пробелы и апострофы
    txt_adresat.setText(uTo);
    //
    String msg = txt_send.getText();
    boolean b;
    b = model.sendMessage(uTo, msg);
    if(b)
      System.out.println(R.Now() + " сообщение отправлено");
    else
      System.err.println(R.Now() + " ошибка отправки сообщения");
  }


  /**
   * прочитать данные о сообщении, выбранном в таблице
   * @param ae  событие
   */
  public void onclick_btn_message(ActionEvent ae)
  {
    TableView.TableViewSelectionModel<Stroka> selectionModel = tbl_senders.getSelectionModel();
    Stroka stro = selectionModel.getSelectedItem();
    if(stro != null) {
      int im = stro.getIm();
      // System.out.println("test " + mind);
      String msg = model.getMsg(im);
      String ufr = model.getFrom(im);
      txt_message.setText(msg);
      txt_adresat.setText(ufr);
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
      Integer iii = Integer.parseInt(r[0]);
      usersData.add(new Stroka(iii,r[1],r[2]));
    }
    //
    col_im.setCellValueFactory(cellData -> cellData.getValue().imProperty());
    col_from.setCellValueFactory(cellData -> cellData.getValue().fromProperty());
    col_dat.setCellValueFactory(cellData -> cellData.getValue().datProperty());
    // заполняем таблицу данными
    tbl_senders.setItems(usersData);
  }

  /**
   * выбрать строку таблицы с указанным индексом сообщения,
   * если указан 0, то выделение на самой первой строке
   * @param im индекс сообщения
   */
  private void selectRow(int im)
  {
    // если фокус ввода в поле набора сообщения, то ничего не делаем
    boolean b = txt_send.isFocused();
    if(b) return;
    TableView.TableViewSelectionModel<Stroka> newselectionModel = tbl_senders.getSelectionModel();
    ObservableList<Stroka> odat = tbl_senders.getItems();
    int n = odat.size();
    for(int i = 0; i < n; i++) {
      Stroka stro = odat.get(i);
      int imi = stro.getIm();
      if(imi == im || im == 0) {
        newselectionModel.select(stro);
        break;
      }
    }
//    getModelItem(i)
//    newselectionModel.select(stro);
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
    Platform.runLater(() -> txt_stdout.appendText(""+b));
  }

} // end of class
