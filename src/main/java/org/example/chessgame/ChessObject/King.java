package org.example.chessgame.ChessObject;

public class King extends ChessPiece {
    public King(Team team) {
        super(team);
    }

    @Override
    boolean checkValidMove(ChessBoard chessBoard, int depX, int depY, int desX, int desY) {
        return false;
    }
}