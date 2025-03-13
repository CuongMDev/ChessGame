package org.example.chessgame.ChessObject;

public class King extends ChessPiece {
    public King(Team team) {
        super(team);
    }

    @Override
    boolean checkValidMove(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);

        // The king moves only one square in any direction
        return dx <= 1 && dy <= 1;
    }
}