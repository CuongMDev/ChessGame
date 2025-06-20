package org.example.chessgame.ChessObject.Move;

import org.example.chessgame.ChessObject.ChessPiece;

public class PreMove {
    public int startX;
    public int startY;
    public int endX;
    public int endY;

    public ChessPiece promotedPiece;

    public PreMove(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }
}
