package org.example.chessgame.ChessObject;

import org.example.chessgame.ChessObject.Move.Move;

public class Pawn extends ChessPiece {
    public Pawn(Team team) {
        super(team);
        createChessImage("pawn");
    }

    public int getSkinCount() {
        return 3;
    }

    @Override
    public boolean checkCanPreMove(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        int direction = (chessBoard.getChessPieceTeam(startX, startY) == Team.WHITE) ? -1 : 1; // White moves up (-1), Black moves down (+1)
        int startRow = (chessBoard.getChessPieceTeam(startX, startY) == Team.WHITE) ? 7 : 2;   // White starts at row 6, Black at row 1

        if (Math.abs(startX - endX) <= 1 && endY == startY + direction) {
            return true;
        }
        // First move: two steps forward
        if (startX == endX && startY == startRow && endY == startY + 2 * direction) {
            return true;
        }

        return false;
    }

    @Override
    public boolean checkValidMove(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        int direction = (chessBoard.getChessPieceTeam(startX, startY) == Team.WHITE) ? -1 : 1; // White moves up (-1), Black moves down (+1)
        int startRow = (chessBoard.getChessPieceTeam(startX, startY) == Team.WHITE) ? 7 : 2;   // White starts at row 6, Black at row 1

        // Normal move: one step forward
        if (startX == endX && endY == startY + direction && !chessBoard.existChessPiece(startX, startY + direction)) {
            return true;
        }

        // First move: two steps forward
        if (startX == endX && startY == startRow && endY == startY + 2 * direction && !chessBoard.existChessPiece(startX, startY + 2 * direction)) {
            return !chessBoard.existChessPiece(startX, startY + direction);
        }


        if (Math.abs(startX - endX) == 1 && startY + direction == endY) {
            // Capturing move: diagonal move
            if (chessBoard.existChessPiece(endX, endY)) {
                if (chessBoard.getChessPieceTeam(endX, endY) != getTeam()) { // Must capture opponent's piece
                    return true;
                }
            } else {
                // En Passant (bắt tốt qua đường)
                if (chessBoard.existChessPiece(endX, endY - direction) && chessBoard.getChessPieceTeam(endX, endY - direction) != getTeam()) {
                    // Kiểm tra nước đi của đối phương có phải là quân tốt di chuyển 2 ô từ vị trí ban đầu
                    Move lastMove = chessBoard.getLastMove(0);
                    if (lastMove != null && chessBoard.getChessPiece(lastMove.endX, lastMove.endY) instanceof Pawn) {
                        // Kiểm tra nếu quân đối phương vừa di chuyển 2 ô
                        if (Math.abs(lastMove.startY - lastMove.endY) == 2 &&
                                lastMove.startX == endX &&
                                chessBoard.getChessPieceTeam(lastMove.endX, lastMove.endY) != getTeam()) {
                            return true; // Nếu thỏa mãn điều kiện en passant
                        }
                    }
                }
            }
        }

        return false; // Any other move is invalid
    }

    @Override
    public boolean checkValidKill(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        int direction = (chessBoard.getChessPieceTeam(startX, startY) == Team.WHITE) ? -1 : 1; // White moves up (-1), Black moves down (+1)
        // Capturing move: diagonal move
        if (Math.abs(startX - endX) == 1 && startY + direction == endY) {
            return chessBoard.getChessPieceTeam(endX, endY) != getTeam(); // Must capture opponent's piece
        }

        return false;
    }
}