package org.example.chessgame.ChessObject.Move;

import org.example.chessgame.ChessObject.ChessPiece;

public class Move {
    public int startX;
    public int startY;
    public int endX;
    public int endY;

    public ChessPiece deadPiece;
    public SpecialMove specialMove;
    public boolean isPreMove;

    public Move(int startX, int startY, int endX, int endY, ChessPiece deadPiece, SpecialMove isSpecialMove, boolean isPreMove) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.deadPiece = deadPiece;
        this.specialMove = isSpecialMove;
        this.isPreMove = isPreMove;
    }
}