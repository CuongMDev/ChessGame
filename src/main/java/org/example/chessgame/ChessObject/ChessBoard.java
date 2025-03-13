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
    private void moveChess(int depX, int depY, int destX, int destY) {
        chessPieceBoard[destX][destY] = chessPieceBoard[depX][depY];
        chessPieceBoard[depX][depY] = null;
    }

    /**
     *
     * @return true if move successfully
     */
    public boolean moveChessPiece(int depX, int depY, int destX, int destY) {
        if (existChessPiece(destX, destY)
                && chessPieceBoard[depX][depY].getTeam() == chessPieceBoard[destX][destY].getTeam()) {
            return false;
        }
        if (chessPieceBoard[depX][depY].checkValidMove(this, depX, depY, destX, destY)) {
            //Check if depChess kill destChess
            if (existChessPiece(destX, destY)
                    && chessPieceBoard[depX][depY].getTeam() != chessPieceBoard[destX][destY].getTeam()) {
                removeChessPiece(destX, destY);
            }

            moveChess(depX, depY, destX, destY);
            return true;
        }

        return false;
    }
}
