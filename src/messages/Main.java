/*
 * Copyright (c) 2020. Eremin
 * 14.03.20 16:35
 *
 */

/*
   Отобразить сообщения текущего пользователя и другого одного пользователя
   а также отправлять ему сообщения
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
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("display.fxml"));
    // Parent root = FXMLLoader.load(getClass().getResource("display.fxml"));
    Parent root = loader.load();
    Controller controller = loader.getController();
    primaryStage.getIcons().add(new Image("res/app.png"));
    primaryStage.setTitle("Обмен сообщениями c пользователем");
    primaryStage.setScene(new Scene(root, 780, 500));
    primaryStage.getScene().getStylesheets().add("css/JMetroLightTheme.css"); //подключим стили
    primaryStage.show();
    controller.setAdresat("ae");
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
}
