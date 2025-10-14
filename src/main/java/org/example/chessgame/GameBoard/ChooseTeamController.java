package org.example.chessgame.GameBoard;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.example.chessgame.Abstract.Controller;
import org.girod.javafx.svgimage.SVGImage;
import org.girod.javafx.svgimage.SVGLoader;


public class ChooseTeamController extends Controller {
    @FXML
    public Button whiteButton;
    @FXML
    public Button blackButton;

    @FXML
    private StackPane whitePane;
    @FXML
    private StackPane blackPane;

    @FXML
    private void initialize() {
        SVGImage whiteImage = SVGLoader.load(getClass().getResource("/org/example/chessgame/ChessObject/images/white-king_0.svg"));
        double whiteScale = Double.min(whitePane.getPrefWidth() / whiteImage.getWidth(), whitePane.getPrefHeight() / whiteImage.getHeight());
        whiteImage.setScaleX(whiteScale);
        whiteImage.setScaleY(whiteScale);
        whitePane.getChildren().add(whiteImage);

        SVGImage blackImage = SVGLoader.load(getClass().getResource("/org/example/chessgame/ChessObject/images/black-king_0.svg"));
        double blackScale = Double.min(blackPane.getPrefWidth() / blackImage.getWidth(), blackPane.getPrefHeight() / blackImage.getHeight());
        blackImage.setScaleX(blackScale);
        blackImage.setScaleY(blackScale);
        blackPane.getChildren().add(blackImage);
    }
}
