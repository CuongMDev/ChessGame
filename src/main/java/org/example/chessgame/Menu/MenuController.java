package org.example.chessgame.Menu;

import javafx.fxml.FXML;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.chessgame.Abstract.Controller;
import org.example.chessgame.ChessObject.ChessBoard;
import org.example.chessgame.GameBoard.GameController;

import java.io.IOException;

public class MenuController extends Controller {
    GameController gameplayController;

    @FXML
    StackPane mainStackPane;
    @FXML
    VBox menuBox;

    @FXML
    public void onPlayWithBotClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            gameplayController.resetGameBoard(ChessBoard.STARTING_FEN);
            mainStackPane.getChildren().setAll(gameplayController.getParent());
            getStage().sizeToScene();
        }
    }
    public void onTwoPlayerClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            gameplayController.resetGameplay(false, true);
        }
    }

    private void initGameplayController() throws IOException {
        gameplayController = (GameController) Controller.init(getStage(), getClass().getResource("/org/example/chessgame/GameBoard/Game.fxml"));
        gameplayController.gameResultController.exitToMenuButton.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                mainStackPane.getChildren().setAll(menuBox);
                getStage().sizeToScene();
            }
        });
    }

    public void closeSocket() {
        gameplayController.closeSocket();
    }

    @FXML
    private void initialize() throws IOException {
        initGameplayController();
    }
}
