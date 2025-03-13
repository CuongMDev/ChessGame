package org.example.chessgame.ChessObject;

public class Pawn extends ChessPiece {
    public Pawn(Team team) {
        super(team);
    }

    @Override
    public boolean checkValidMove(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        int direction = (chessBoard.getChessPieceTeam(startX, startY) == getTeam()) ? -1 : 1; // White moves up (-1), Black moves down (+1)
        int startRow = (chessBoard.getChessPieceTeam(startX, startY) == getTeam()) ? 6 : 1;   // White starts at row 6, Black at row 1

        // Normal move: one step forward
        if (startX == endX && endY == startY + direction) {
            return true;
        }

        // First move: two steps forward
        if (startX == endX && startY == startRow && endY == startY + 2 * direction) {
            return !chessBoard.existChessPiece(startX, startY + direction);
        }

        // Capturing move: diagonal move
        if (Math.abs(startX - endX) == 1 && startY == endY + direction) {
            return chessBoard.existChessPiece(endX, endY) && chessBoard.getChessPieceTeam(endX, endY) != getTeam(); // Must capture opponent's piece
        }

        return false; // Any other move is invalid
    }
}