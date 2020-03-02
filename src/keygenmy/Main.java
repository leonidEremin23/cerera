package keygenmy;
/*
 Формирование собственного ключа для получателя
 */

import ae.R;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("keygenmy.fxml"));
    primaryStage.setTitle("Регистрация нового пользователя");
    primaryStage.setScene(new Scene(root, 700, 480));
    //primaryStage.getScene().getStylesheets().add("css/JMetroLightTheme.css");
    primaryStage.show();
  }

  public static void main(String[] args) {
    R.loadDefault();
    launch(args);
  }

} // end class

