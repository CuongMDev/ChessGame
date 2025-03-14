package org.example.chessgame.ChessObject;

public class Bishop extends ChessPiece {
    public Bishop(Team team) {
        super(team);
        createChessImage("bishop", 1);
    }

    private boolean isPathClear(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        int stepX = Integer.compare(endX, startX); // -1 or 1
        int stepY = Integer.compare(endY, startY); // -1 or 1

        int x = startX + stepX;
        int y = startY + stepY;

        while (x != endX || y != endY) {
            if (chessBoard.existChessPiece(x, y)) {
                return false;
            }
            x += stepX;
            y += stepY;
        }

        return true;
    }

    @Override
    boolean checkValidMove(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);

        // The bishop moves diagonally, meaning dx must equal dy
        if (dx != dy) {
            return false;
        }

        // Check for obstacles in the path
        return isPathClear(chessBoard, startX, startY, endX, endY);
    }
}