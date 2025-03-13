package org.example.chessgame.ChessObject;

public class Pawn extends ChessPiece {
    public Pawn(Team team) {
        super(team);
    }

    @Override
    public boolean checkValidMove(ChessBoard chessBoard, int depX, int depY, int desX, int desY) {
        return false;
    }
}