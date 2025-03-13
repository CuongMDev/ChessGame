package org.example.chessgame.ChessObject;

public class Bishop extends ChessPiece {
    public Bishop(Team team) {
        super(team);
    }

    @Override
    boolean checkValidMove(ChessBoard chessBoard, int depX, int depY, int desX, int desY) {
        return false;
    }
}