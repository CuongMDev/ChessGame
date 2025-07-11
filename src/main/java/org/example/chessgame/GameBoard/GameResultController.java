package org.example.chessgame.GameBoard;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.chessgame.Abstract.Controller;

public class GameResultController extends Controller {
    @FXML
    private Label resultLabel;

    @FXML
    public Button rematchButton;
    @FXML
    public Button exitToMenuButton;

    public void setResult(String result) {
        resultLabel.setText(result);
    }
}
