package org.example.chessgame.ChessObject;

public abstract class ChessPiece {
    public enum Team {
        WHILE, BLACK
    }

    private final Team team;

    public ChessPiece(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    /**
     * Check if exist chess pieces between two chess pieces.
     * Check if path is valid.
     * No need to check exist chess pieces in start or end position.
     */
    abstract boolean checkValidMove(ChessBoard chessBoard, int startX, int startY, int endX, int endY);
}