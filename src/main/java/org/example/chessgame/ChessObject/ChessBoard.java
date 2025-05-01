package org.example.chessgame.ChessObject;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class ChessBoard {
    private ChessPiece[][] chessPieceBoard;
    private int[][] kingPosition;
    private Stack<Move> history = new Stack<Move>();


    public ChessBoard() {
        //Start from 1 -> 8
        chessPieceBoard = new ChessPiece[9][9];
        kingPosition = new int[2][2];
    }

    private void initChessPosition() {
        for (int i = 1; i <= 8; i++) {
            Arrays.fill(chessPieceBoard[i], null);
        }

        // Pawn
        for (int x = 1; x <= 8; x++) {
            setChessPiece(x, 7, new Pawn(ChessPiece.Team.WHITE));
            setChessPiece(x, 2, new Pawn(ChessPiece.Team.BLACK));
        }

        // Castle
        setChessPiece(1, 8, new Rook(ChessPiece.Team.WHITE));
        setChessPiece(8, 8, new Rook(ChessPiece.Team.WHITE));

        setChessPiece(1, 1, new Rook(ChessPiece.Team.BLACK));
        setChessPiece(8, 1, new Rook(ChessPiece.Team.BLACK));

        // Knight
        setChessPiece(2, 8, new Knight(ChessPiece.Team.WHITE));
        setChessPiece(7, 8, new Knight(ChessPiece.Team.WHITE));

        setChessPiece(2, 1, new Knight(ChessPiece.Team.BLACK));
        setChessPiece(7, 1, new Knight(ChessPiece.Team.BLACK));

        // Bishop
        setChessPiece(3, 8, new Bishop(ChessPiece.Team.WHITE));
        setChessPiece(6, 8, new Bishop(ChessPiece.Team.WHITE));

        setChessPiece(3, 1, new Bishop(ChessPiece.Team.BLACK));
        setChessPiece(6, 1, new Bishop(ChessPiece.Team.BLACK));

        // Queen
        setChessPiece(4, 8, new Queen(ChessPiece.Team.WHITE));
        setChessPiece(4, 1, new Queen(ChessPiece.Team.BLACK));

        // King
        setChessPiece(5, 8, new King(ChessPiece.Team.WHITE));
        setChessPiece(5, 1, new King(ChessPiece.Team.BLACK));
    }

    public void initChessBoard() {
        initChessPosition();
    }

    public ChessPiece.Team getChessPieceTeam(int x, int y) {
        return chessPieceBoard[y][x].getTeam();
    }

    public ChessPiece getChessPiece(int x, int y) {
        return chessPieceBoard[y][x];
    }

    public boolean existChessPiece(int x, int y) {
        return chessPieceBoard[y][x] != null;
    }

    public void removeChessPiece(int x, int y) {
        chessPieceBoard[y][x] = null;
    }

    public boolean isOutside(int x, int y) {
        return (x < 1 || x > 8 || y < 1 || y > 8);
    }

    public void setChessPiece(int x, int y, ChessPiece chessPiece) {
        chessPieceBoard[y][x] = chessPiece;
        if (chessPiece == null) {
            return;
        }

        if (chessPiece instanceof King) {
            kingPosition[chessPiece.getTeam().ordinal()][0] = x;
            kingPosition[chessPiece.getTeam().ordinal()][1] = y;
        }
    }

    public List<int[]> getValidMoves(int x, int y) {
        if (getChessPiece(x, y) == null) {
            return null;
        }

        List<int[]> moves = new ArrayList<>();
        for (int endX = 1; endX <= 8; endX++) {
            for (int endY = 1; endY <= 8; endY++) {
                if (endX != x && endY != y && existChessPiece(endX, endY) && getChessPiece(endX, endY).checkValidMove(this, x, y, endX, endY)) {
                    moves.add(new int[]{endX, endY});
                }
            }
        }
        return moves;
    }

    public void setPromote(int endX, int endY, ChessPiece chessPiece) {
        // Lưu
        moveChess(endX, endY, 0, 0);
        setChessPiece(endX, endY, chessPiece);
        getLastMove(0).isSpecialMove = true;
    }

    /**
     * not check and just move chess piece
     */
    private void moveChess(int startX, int startY, int endX, int endY) {
        history.add(new Move(startX, startY, endX, endY, getChessPiece(endX, endY), false));

        setChessPiece(endX, endY, getChessPiece(startX, startY));
        setChessPiece(startX, startY, null);

        getChessPiece(endX, endY).changeMoveNumber(1);
    }

    public Move getLastMove(int lastNumber) {
        return history.get(history.size() - 1 - lastNumber);
    }

    private Move rollbackOne() {
        if (history.isEmpty()) {
            return null;
        }
        Move lastMove = history.pop();

        getChessPiece(lastMove.endX, lastMove.endY).changeMoveNumber(-1);
        setChessPiece(lastMove.startX, lastMove.startY, getChessPiece(lastMove.endX, lastMove.endY));
        setChessPiece(lastMove.endX, lastMove.endY, lastMove.deadPiece);

        return lastMove;
    }

    public List<Move> rollback() {
        List<Move> moves = new ArrayList<>();

        Move lastMove = rollbackOne();
        if (lastMove == null) {
            return null;
        }
        moves.add(lastMove);

        if (lastMove.isSpecialMove) {
            moves.add(rollbackOne());
        }

        return moves;
    }

    private boolean checkKingCanMove(ChessPiece.Team team) {
        int kingX = kingPosition[team.ordinal()][0];
        int kingY = kingPosition[team.ordinal()][1];
        var kingValidMoves = getValidMoves(kingX, kingY);
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                for (var kingMove : kingValidMoves) {
                    if (existChessPiece(x, y) && getChessPieceTeam(x, y) != team && getChessPiece(x, y).checkValidKill(this, x, y, kingMove[0], kingMove[1])) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean isUnderAttack(int chessX, int chessY, ChessPiece.Team team) {
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                if (existChessPiece(x, y) && getChessPieceTeam(x, y) != team && getChessPiece(x, y).checkValidKill(this, x, y, chessX, chessY)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkKingInCheck(ChessPiece.Team team) {
        int kingX = kingPosition[team.ordinal()][0];
        int kingY = kingPosition[team.ordinal()][1];
        return isUnderAttack(kingX, kingY, team);
    }

    private boolean checkCanMovePiece(int startX, int startY, int endX, int endY) {
        if (existChessPiece(endX, endY)
                && getChessPieceTeam(startX, startY) == getChessPieceTeam(endX, endY)) {
            return false;
        }

        ChessPiece.Team team = getChessPieceTeam(startX, startY);
        if (getChessPiece(startX, startY).checkValidMove(this, startX, startY, endX, endY)) {
            // Simulation
            moveChess(startX, startY, endX, endY);
            if (checkKingInCheck(team)) {
                rollback();
                return false;
            }

            rollback();
            return true;
        }

        return false;
    }

    public boolean checkPromotion(int endX, int endY) {
        // Kiểm tra phong
        if (getChessPiece(endX, endY) instanceof Pawn) {
            return (getChessPieceTeam(endX, endY) == ChessPiece.Team.WHITE && endY == 1) ||
                    (getChessPieceTeam(endX, endY) == ChessPiece.Team.BLACK && endY == 8);
        }

        return false;
    }

    /**
     *
     * @return true if move successfully and move piece
     */
    public boolean moveChessPiece(int startX, int startY, int endX, int endY) {
        if (checkCanMovePiece(startX, startY, endX, endY)) {
            boolean specialMove = false;
            // Kiểm tra nhập thành
            if (getChessPiece(startX, startY) instanceof King && !getChessPiece(startX, startY).checkValidKill(this, startX, startY, endX, endY)) {
                // Di chuyển xe
                if (endX == 3) {
                    moveChess(1, startY, startX - 1, endY);
                } else {
                    moveChess(8, startY, startX + 1, endY);
                }
                specialMove = true;
            }
            // Kiểm tra bắt tốt qua đường
            int direction = (getChessPieceTeam(startX, startY) == ChessPiece.Team.WHITE) ? -1 : 1; // White moves up (-1), Black moves down (+1)
            if (getChessPiece(startX, startY) instanceof Pawn && Math.abs(startX - endX) == 1 && startY + direction == endY && !existChessPiece(endX, endY)) {
                moveChess(endX, endY - direction, 0, 0);
                specialMove = true;
            }

            moveChess(startX, startY, endX, endY);

            if (specialMove) {
                getLastMove(0).isSpecialMove = true;
            }

            return true;
        }

        return false;
    }
}
