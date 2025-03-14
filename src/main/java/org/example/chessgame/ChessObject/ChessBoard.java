package org.example.chessgame.ChessObject;

import java.util.Arrays;

public class ChessBoard {
    private ChessPiece[][] chessPieceBoard;

    public ChessBoard() {
        //Start from 1 -> 8
        chessPieceBoard = new ChessPiece[9][9];
    }

    private void initChessPosition() {
        for (int i = 1; i <= 8; i++) {
            Arrays.fill(chessPieceBoard[i], null);
        }

        // Pawn
        for (int x = 1; x <= 8; x++) {
            chessPieceBoard[x][7] = new Pawn(ChessPiece.Team.WHILE);
            chessPieceBoard[x][2] = new Pawn(ChessPiece.Team.BLACK);
        }

        // Castle
        chessPieceBoard[1][8] = new Rook(ChessPiece.Team.WHILE);
        chessPieceBoard[8][8] = new Rook(ChessPiece.Team.WHILE);

        chessPieceBoard[1][1] = new Rook(ChessPiece.Team.BLACK);
        chessPieceBoard[8][1] = new Rook(ChessPiece.Team.BLACK);

        // Knight
        chessPieceBoard[2][8] = new Knight(ChessPiece.Team.WHILE);
        chessPieceBoard[7][8] = new Knight(ChessPiece.Team.WHILE);

        chessPieceBoard[2][1] = new Knight(ChessPiece.Team.BLACK);
        chessPieceBoard[7][1] = new Knight(ChessPiece.Team.BLACK);

        // Bishop
        chessPieceBoard[3][8] = new Bishop(ChessPiece.Team.WHILE);
        chessPieceBoard[6][8] = new Bishop(ChessPiece.Team.WHILE);

        chessPieceBoard[3][1] = new Bishop(ChessPiece.Team.BLACK);
        chessPieceBoard[6][1] = new Bishop(ChessPiece.Team.BLACK);

        // Queen
        chessPieceBoard[4][8] = new Queen(ChessPiece.Team.WHILE);
        chessPieceBoard[4][1] = new Queen(ChessPiece.Team.BLACK);

        // King
        chessPieceBoard[5][8] = new King(ChessPiece.Team.WHILE);
        chessPieceBoard[5][1] = new King(ChessPiece.Team.BLACK);
    }

    public void initChessBoard() {
        initChessPosition();
    }

    public ChessPiece.Team getChessPieceTeam(int x, int y) {
        return chessPieceBoard[x][y].getTeam();
    }

    public ChessPiece getChessPiece(int x, int y) {
        return chessPieceBoard[x][y];
    }

    public boolean existChessPiece(int x, int y) {
        return chessPieceBoard[x][y] != null;
    }

    public void removeChessPiece(int x, int y) {
        chessPieceBoard[x][y] = null;
    }

    public boolean isOutside(int x, int y) {
        return (x < 1 || x > 8 || y < 1 || y > 8);
    }

    /**
     * not check and just move chess piece
     */
    private void moveChess(int startX, int startY, int endX, int endY) {
        chessPieceBoard[endX][endY] = chessPieceBoard[startX][startY];
        chessPieceBoard[startX][startY] = null;
    }

    /**
     *
     * @return true if move successfully
     */
    public boolean moveChessPiece(int startX, int startY, int endX, int endY) {
        if (existChessPiece(endX, endY)
                && getChessPieceTeam(startX, startY) == getChessPieceTeam(endX, endY)) {
            return false;
        }
        if (chessPieceBoard[startX][startY].checkValidMove(this, startX, startY, endX, endY)) {
            //Check if startChess kill endChess
            if (existChessPiece(endX, endY)
                    && chessPieceBoard[startX][startY].getTeam() != chessPieceBoard[endX][endY].getTeam()) {
                removeChessPiece(endX, endY);
            }

            moveChess(startX, startY, endX, endY);
            return true;
        }

        return false;
    }
}
