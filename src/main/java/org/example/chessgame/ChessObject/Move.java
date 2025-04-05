package org.example.chessgame.ChessObject;

public class Move {
    public int startX;
    public int startY;
    public int endX;
    public int endY;

    public ChessPiece deadPiece;
    public int moveNumber;
    public boolean isSpecialMove;

    public Move(int startX, int startY, int endX, int endY, ChessPiece deadPiece, boolean isSpecialMove) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.deadPiece = deadPiece;
        this.isSpecialMove = isSpecialMove;
    }
}