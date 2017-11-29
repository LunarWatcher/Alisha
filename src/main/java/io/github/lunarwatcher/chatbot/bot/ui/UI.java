package io.github.lunarwatcher.chatbot.bot.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import io.github.lunarwatcher.chatbot.Database;
import io.github.lunarwatcher.chatbot.bot.Bot;
import io.github.lunarwatcher.chatbot.bot.sites.Chat;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

/**
 * TODO Note to self: Using the launch method inherited from application starts the actual UI. call at the end of the main method
 *
 * Gotta get back to the UI later, the SE network isn't working correctly any more. Bot first, UI second :D
 */
public class UI extends Application {

    public static Properties botProps;
    public static Database database;
    public static Bot bot;

    public UI(){

    }

    public UI(String[] args){
        launch(args);
    }
    @Override
    public void start(Stage stage) throws Exception {

        JFXListView<String> sites = new JFXListView<>();

        for(Chat c : bot.getChats()){
            sites.getItems().add(c.getName());
        }

        AnchorPane root = new AnchorPane();

        JFXButton start = new JFXButton("Start");
        JFXButton terminate = new JFXButton("Terminate");
        JFXButton globalKill = new JFXButton("Global shutdown");

        root.getChildren().add(start);
        root.getChildren().add(terminate);
        root.getChildren().add(globalKill);
        root.getChildren().add(sites);

        Scene scene = new Scene(root, 600, 600, javafx.scene.paint.Color.WHITE);
        stage.setTitle("Alisha");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();



    }
}
