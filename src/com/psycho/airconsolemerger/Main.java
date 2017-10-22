/*
 * Copyright (c) Psychokiller1888 - Sick Rabbit Studios 2017.
 * You may not redistribute this file. You may not sell part or the entire file
 */

package com.psycho.airconsolemerger;

import com.psycho.airconsolemerger.view.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public class Main extends Application {

    private Properties _properties;

    @Override
    public void start(Stage primaryStage) throws Exception{
        loadProperties();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("view/main.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("AirConsole Construct 2/3 project merger v.1.2.1");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        primaryStage.show();

        MainController mainController = loader.getController();
        mainController.setMain(this);
        mainController.setStage(primaryStage);
        mainController.init();
    }

    @Override
    public void stop() {
        saveProperties();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void saveProperties() {
        OutputStream out = null;

        try {
            out = new FileOutputStream("settings.properties");
            _properties.store(out, null);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    private void loadProperties() {
        _properties = new Properties();

        InputStream in = null;
        File settings = new File("settings.properties");

        try {
            if (settings.exists()) {
                in = new FileInputStream(settings);
            }
            else {
                in = getClass().getResourceAsStream("settings.properties");
                if (in == null) {
                    System.out.println("Can't load settings");
                    return;
                }
            }

            _properties.load(in);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    public void setProperty(String property, String value) {
        _properties.setProperty(property, value);
    }

    public String getPropertyValue(String property) {
        return _properties.getProperty(property, null);
    }
}