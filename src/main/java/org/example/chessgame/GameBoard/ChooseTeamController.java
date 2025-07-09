package org.example.chessgame.GameBoard;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.AnchorPane;
import org.example.chessgame.Abstract.Controller;

public class ChooseTeamController extends Controller {
    @FXML
    public Button whiteButton;
    @FXML
    public Button blackButton;

    @FXML
    private AnchorPane mainPane;

    @FXML
    private void initialize() {
    }
}
