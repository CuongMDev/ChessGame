package org.example.chessgame.Menu;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.chessgame.Abstract.Controller;
import org.example.chessgame.ChessObject.ChessBoard;
import org.example.chessgame.GameBoard.GameController;
import org.example.chessgame.Menu.Setting.SettingController;

import java.io.IOException;

public class MenuController extends Controller {
    GameController gameplayController;
    SettingController settingController;

    @FXML
    TextField fenTextField;
    @FXML
    StackPane mainStackPane;
    @FXML
    VBox menuBox;

    private boolean checkValidFen() {
        String fen = fenTextField.getText();
        if (fen.isEmpty()) {
            fenTextField.getStyleClass().remove("invalid");
            return true;
        }
        if (ChessBoard.isValidFEN(fen)) {
            fenTextField.getStyleClass().remove("invalid");
            return true;
        }

        if (!fenTextField.getStyleClass().contains("invalid")) {
            fenTextField.getStyleClass().add("invalid");
        }
        return false;
    }

    private String getFen() {
        String fen = fenTextField.getText();
        if (fen.isEmpty()) {
            fen = ChessBoard.STARTING_FEN;
        }
        return fen;
    }

    @FXML
    private void onPlayWithBotClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (!checkValidFen()) {
                return;
            }
            gameplayController.resetGameBoard(true, getFen());
            gameplayController.setThinkingAbility(settingController.thinkingAbilitySlider.getValue(), settingController.searchThreadSlider.getValue());
            gameplayController.gameSound.setVolume(settingController.gameMusicSlider.getValue());
            mainStackPane.getChildren().setAll(gameplayController.getParent());
            getStage().sizeToScene();
        }
    }

    @FXML
    private void onTwoPlayerClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (!checkValidFen()) {
                return;
            }
            gameplayController.gameSound.setVolume((int) settingController.gameMusicSlider.getValue());
            gameplayController.resetGameBoard(false, getFen());
            mainStackPane.getChildren().setAll(gameplayController.getParent());
            getStage().sizeToScene();
        }
    }

    @FXML
    private void onSettingClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            mainStackPane.getChildren().setAll(settingController.getParent());
        }
    }

    @FXML
    private void onExitClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            Platform.exit();
        }
    }

    private void initControllers() throws IOException {
        // gameplay
        gameplayController = (GameController) Controller.init(getStage(), getClass().getResource("/org/example/chessgame/GameBoard/Game.fxml"));
        gameplayController.gameResultController.exitToMenuButton.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                mainStackPane.getChildren().setAll(menuBox);
                getStage().sizeToScene();
            }
        });

        // setting
        settingController = (SettingController) Controller.init(getStage(), getClass().getResource("/org/example/chessgame/Setting/Setting.fxml"));
        settingController.okButton.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                mainStackPane.getChildren().setAll(menuBox);
                settingController.saveSettings();
            }
        });
    }

    public void closeSocket() {
        gameplayController.closeSocket();
    }

    @FXML
    private void initialize() throws IOException {
        initControllers();
    }
}
