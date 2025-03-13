package org.example.chessgame.ChessObject;

public class Castle extends ChessPiece {
    public Castle(Team team) {
        super(team);
    }

    @Override
    boolean checkValidMove(ChessBoard chessBoard, int depX, int depY, int desX, int desY) {
        if (depX != desX && depY != desY) {
            return false;
        }
        //Swap
        if (depX > desX) {
            int temp = desX;
            desX = depX;
            depX = temp;
        }
        if (depY > desY) {
            int temp = desY;
            desY = depY;
            depY = temp;
        }

        if (depX == desX) {
            for (int y = depY; y < desY; y++) {
                if (chessBoard.existChessPiece(depX, y)) {
                    return false;
                }
            }
            return true;
        }
        //depY == desY
        for (int x = depX; x < desX; x++) {
            if (chessBoard.existChessPiece(x, depY)) {
                return false;
            }
        }
        return true;
    }
}