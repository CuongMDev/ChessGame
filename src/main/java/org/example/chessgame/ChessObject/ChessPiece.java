package org.example.chessgame.ChessObject;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;
import java.util.Random;

public abstract class ChessPiece {
    public enum Team {
        WHITE, BLACK
    }

    private final Team team;
    private final Random randSkin = new Random();
    private static final String[] teamString = {"white", "black"};
    private static final String chessImagePath = "images/";
    private int moveNumber;

    ImageView chessImage;

    public abstract int getSkinCount();

    protected void createChessImage(String imageName) {
        int skinType = randSkin.nextInt(getSkinCount());
        String path = String.format("%s%s-%s_%d.png", chessImagePath, teamString[getTeam().ordinal()], imageName, skinType);

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

    public void changeMoveNumber(int value) {
        this.moveNumber += value;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

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