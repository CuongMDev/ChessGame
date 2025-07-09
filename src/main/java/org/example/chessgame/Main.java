package org.example.chessgame;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.chessgame.Abstract.Controller;
import org.example.chessgame.GameBoard.GameController;
import org.example.chessgame.Menu.MenuController;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        MenuController menuController = (MenuController) Controller.init(stage, getClass().getResource("Menu/Menu.fxml"));
        stage.setTitle("Chess!");
        stage.setScene(menuController.getParent().getScene());
        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest(event -> {
            menuController.closeSocket();
        });
    }

    public static void main(String[] args) {
        launch();
    }
}