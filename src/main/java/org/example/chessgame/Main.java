package org.example.chessgame;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.chessgame.Abstract.Controller;
import org.example.chessgame.GameBoard.GameController;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        GameController gameController = (GameController) Controller.init(stage, getClass().getResource("GameBoard/Game.fxml"));
        stage.setTitle("Chess!");
        stage.setScene(gameController.getParent().getScene());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}