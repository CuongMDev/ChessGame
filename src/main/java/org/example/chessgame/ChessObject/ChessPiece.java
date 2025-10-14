package org.example.chessgame.ChessObject;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.girod.javafx.svgimage.SVGImage;
import org.girod.javafx.svgimage.SVGLoader;

import java.net.URL;
import java.util.Random;

public abstract class ChessPiece {
    public enum Team {
        WHITE, BLACK
    }

    private final static int CHESS_WIDTH = 33;
    private final static double CHESS_SIZE_RATIO = 0.75;

    private final Team team;
    private final Random randSkin = new Random();
    private static final String[] teamString = {"white", "black"};
    private static final String chessImagePath = "images/";
    private int moveNumber;

    StackPane chessImage;

    public abstract int getSkinCount();

    protected void createChessImage(String imageName) {
        int skinType = randSkin.nextInt(getSkinCount());
        String path = String.format("%s%s-%s_%d.svg", chessImagePath, teamString[getTeam().ordinal()], imageName, skinType);
        URL url = getClass().getResource(path);

        SVGImage svgImage = SVGLoader.load(url);
        chessImage = createSVGWrapper(svgImage);

        chessImage.setPickOnBounds(true); // Nhận cả phần rìa
    }

    public ChessPiece(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public StackPane getChessImage() {
        return chessImage;
    }

    public static StackPane createCopyImage(StackPane chessImage, Pane container) {
        SVGImage copySVG = SVGLoader.load(((SVGImage)chessImage.getChildren().getFirst()).getSVGContent().url);
        StackPane wrapper = createSVGWrapper(copySVG);

        wrapper.prefWidthProperty().bind(container.widthProperty());
        wrapper.prefHeightProperty().bind(container.heightProperty());

        return wrapper;
    }

    private static StackPane createSVGWrapper(SVGImage svgImage) {
        StackPane wrapper = new StackPane();
        DoubleBinding binding = wrapper.widthProperty().divide(CHESS_WIDTH).multiply(CHESS_SIZE_RATIO);

        svgImage.scaleXProperty().bind(binding);
        svgImage.scaleYProperty().bind(binding);

        wrapper.getChildren().add(svgImage);

        return wrapper;
    }

    public void changeMoveNumber(int value) {
        this.moveNumber += value;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public abstract boolean checkCanPreMove(ChessBoard chessBoard, int startX, int startY, int endX, int endY);

    /**
     * Check if exist chess pieces between two chess pieces.
     * Check if path is valid.
     * No need to check exist chess pieces in start or end position.
     */
    public boolean checkValidMove(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        return checkValidKill(chessBoard, startX, startY, endX, endY);
    }

    /**
     * Check if exist chess pieces between two chess pieces.
     * Check if chess piece can kill other chess piece if exist enemy in end position.
     * No need to check exist chess pieces in start or end position.
     */
    public abstract boolean checkValidKill(ChessBoard chessBoard, int startX, int startY, int endX, int endY);
}