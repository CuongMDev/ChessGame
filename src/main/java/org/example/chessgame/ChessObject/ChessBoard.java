package org.example.chessgame.ChessObject;

import java.util.Arrays;

public class ChessBoard {
    private ChessPiece[][] chessPieceBoard;

    public ChessBoard() {
        chessPieceBoard = new ChessPiece[8][8];
    }

    private void initChessPosition() {
        Arrays.fill(chessPieceBoard, null);

        //Pawn
        for (int x = 0; x < 8; x++) {
            chessPieceBoard[x][1] = new Pawn(ChessPiece.Team.WHILE);
            chessPieceBoard[x][6] = new Pawn(ChessPiece.Team.BLACK);
        }

        //Castle
        chessPieceBoard[0][0] = new Castle(ChessPiece.Team.WHILE);
        chessPieceBoard[7][0] = new Castle(ChessPiece.Team.WHILE);

        chessPieceBoard[0][7] = new Castle(ChessPiece.Team.BLACK);
        chessPieceBoard[7][7] = new Castle(ChessPiece.Team.BLACK);

        //Knight
        chessPieceBoard[1][0] = new Knight(ChessPiece.Team.WHILE);
        chessPieceBoard[6][0] = new Knight(ChessPiece.Team.WHILE);

        chessPieceBoard[1][7] = new Knight(ChessPiece.Team.BLACK);
        chessPieceBoard[6][7] = new Knight(ChessPiece.Team.BLACK);

        //Bishop
        chessPieceBoard[2][0] = new Bishop(ChessPiece.Team.WHILE);
        chessPieceBoard[5][0] = new Bishop(ChessPiece.Team.WHILE);

        chessPieceBoard[2][7] = new Bishop(ChessPiece.Team.BLACK);
        chessPieceBoard[5][7] = new Bishop(ChessPiece.Team.BLACK);

        //Queen
        chessPieceBoard[3][0] = new Queen(ChessPiece.Team.WHILE);
        chessPieceBoard[4][7] = new Queen(ChessPiece.Team.BLACK);

        //King
        chessPieceBoard[4][0] = new King(ChessPiece.Team.WHILE);
        chessPieceBoard[3][7] = new King(ChessPiece.Team.BLACK);
    }

    public void initChessBoard() {
        initChessPosition();
    }

    public ChessPiece.Team getChessPieceTeam(int x, int y) {
        return chessPieceBoard[x][y].getTeam();
    }

    public boolean existChessPiece(int x, int y) {
        return chessPieceBoard[x][y] != null;
    }

    public void removeChessPiece(int x, int y) {
        chessPieceBoard[x][y] = null;
    }

    public boolean checkOutside(int x, int y) {
        return (x >= 0 && x < 8 && y >= 0 && y < 8);
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
                && chessPieceBoard[startX][startY].getTeam() == chessPieceBoard[endX][endY].getTeam()) {
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
