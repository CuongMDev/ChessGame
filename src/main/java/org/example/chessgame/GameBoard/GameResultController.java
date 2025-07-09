package org.example.chessgame.GameBoard;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;
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
