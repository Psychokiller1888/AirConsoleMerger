package com.psycho.airconsolemerger;

import com.psycho.airconsolemerger.view.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("view/main.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("AirConsole Construct 2/3 project merger");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        primaryStage.show();

        MainController mainController = loader.getController();
        mainController.setMain(this);
        mainController.setStage(primaryStage);
        mainController.init();
    }

    public static void main(String[] args) {
        launch(args);
    }
}