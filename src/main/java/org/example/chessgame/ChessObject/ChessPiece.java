package org.example.chessgame.ChessObject;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ChessPiece {
    public enum Team {
        WHILE, BLACK
    }

    private final Team team;
    private final Random randSkin = new Random();
    private static final String[] teamString = {"white", "black"};
    private static final String chessImageBoard = "images/";

    ImageView chessImage;

    protected void createChessImage(String imageName, int skinCount) {
        int skinType = randSkin.nextInt(skinCount);
        String path = String.format("%s%s-%s_%d.png", chessImageBoard, teamString[getTeam().ordinal()], imageName, skinType);

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path))); // Đường dẫn ảnh
        chessImage = new ImageView(image);
        chessImage.setPreserveRatio(true); // Giữ tỷ lệ ảnh
        chessImage.setPickOnBounds(true); // Nhận cả phần rìa
    }

    public ChessPiece(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public ImageView getChessImage() {
        return chessImage;
    }

    /**
     * Check if exist chess pieces between two chess pieces.
     * Check if path is valid.
     * No need to check exist chess pieces in start or end position.
     */
    abstract boolean checkValidMove(ChessBoard chessBoard, int startX, int startY, int endX, int endY);
}