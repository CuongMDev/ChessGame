package org.example.chessgame.ChessObject;

public class Rook extends ChessPiece {
    public Rook(Team team) {
        super(team);
        createChessImage("rook");
    }

    public int getSkinCount() {
        return 1;
    }

    // Check if the path between (startX, startY) and (endX, endY) is clear
    private boolean isPathClear(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        int stepX = Integer.compare(endX, startX); // -1, 0, 1
        int stepY = Integer.compare(endY, startY); // -1, 0, 1

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
        // The rook moves only in a straight line (either horizontally or vertically)
        if (startX != endX && startY != endY) {
            return false;
        }

        // Check for obstacles in the path
        return isPathClear(chessBoard, startX, startY, endX, endY);
    }
}