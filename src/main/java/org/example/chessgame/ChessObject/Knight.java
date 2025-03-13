package org.example.chessgame.ChessObject;

public class Knight extends ChessPiece {
    public Knight(Team team) {
        super(team);
    }

    @Override
    boolean checkValidMove(ChessBoard chessBoard, int depX, int depY, int desX, int desY) {
        return false;
    }
}