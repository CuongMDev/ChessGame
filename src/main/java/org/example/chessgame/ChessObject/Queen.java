package org.example.chessgame.ChessObject;

public class Queen extends ChessPiece {
    public Queen(Team team) {
        super(team);
    }

    @Override
    boolean checkValidMove(ChessBoard chessBoard, int depX, int depY, int desX, int desY) {
        return false;
    }
}