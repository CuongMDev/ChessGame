package org.example.chessgame.ChessObject;

public abstract class ChessPiece {
    public static enum Team {
        WHILE, BLACK
    }

    private Team team;

    public ChessPiece(Team team) {
        this.team = team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    abstract boolean checkValidMove(ChessBoard chessBoard, int depX, int depY, int desX, int desY);
}