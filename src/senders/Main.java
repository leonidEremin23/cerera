/*
 * Copyright (c) 2020. Eremin
 * 13.03.20 12:14
 *
 */
/*
    Cписок отправителей сообщений (через web-сервер)
 */

package senders;

import ae.R;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

  public static void main(String[] args) {
    if (args.length > 0) {
      R.setWorkDb(args[0]); // первый аргумент командной строки - имя файла БД
    }
    R.loadDefault();
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("senders.fxml"));
    primaryStage.getIcons().add(new Image("res/app.png"));
    primaryStage.setTitle("Обмен зашифрованными сообщениями");
    primaryStage.setScene(new Scene(root, 780, 500));
    primaryStage.getScene().getStylesheets().add("css/JMetroLightTheme.css"); //подключим стили
    primaryStage.show();
  }

  /**
   * Закрытие приложения
   */
  @Override
  public void stop() throws Exception {
    R.dbClose();
    super.stop();
  }

} // end of class
