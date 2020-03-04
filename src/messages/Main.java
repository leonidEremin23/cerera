/*
 * Copyright (c) 2020. Eremin
 * 01.03.20 15:18
 *
 */

/*
   Месеенджер для обмена зашифрованными сообщениями
 */
package messages;

import ae.R;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception{
    Parent root = FXMLLoader.load(getClass().getResource("messages.fxml"));
    primaryStage.getIcons().add(new Image("res/app.png"));
    primaryStage.setTitle("Обмен зашифрованными сообщениями");
    primaryStage.setScene(new Scene(root, 900, 500));
    primaryStage.getScene().getStylesheets().add("css/JMetroLightTheme.css"); //подключим стили
    primaryStage.show();
  }

  public static void main(String[] args) {
    if (args.length > 0) {
      R.setWorkDb(args[0]); // первый аргумент командной строки - имя файла БД
    }
    R.loadDefault();
    launch(args);
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
