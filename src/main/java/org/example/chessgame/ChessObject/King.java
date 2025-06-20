package org.example.chessgame.ChessObject;

public class King extends ChessPiece {
    public King(Team team) {
        super(team);
        createChessImage("king");
    }

    @Override
    public int getSkinCount() {
        return 2;
    }

    @Override
    public boolean checkCanPreMove(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        if (checkValidKill(chessBoard, startX, startY, endX, endY)) {
            return true;
        }
        int rank = (getTeam() == Team.WHITE) ? 8 : 1; // Hàng của vua tùy theo màu
        // Kiểm tra nếu đang đứng đúng vị trí vua ban đầu
        if (startY == rank && startX == 5 && endY == rank) {
            // Nhập thành Kingside (vua đi sang g7 -> x = 7)
            if (endX == 7) {
                // Kiểm tra xe đã di chuyển chưa
                if (!chessBoard.existChessPiece(8, rank)
                        || chessBoard.getChessPiece(8, rank).getMoveNumber() > 0) return false;
                return true;
            }

            // Nhập thành Queenside (vua đi sang c1/c8 -> x = 3)
            if (endX == 3) {
                // Kiểm tra xe đã di chuyển chưa
                if (!chessBoard.existChessPiece(1, rank)
                        || chessBoard.getChessPiece(1, rank).getMoveNumber() > 0) return false;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean checkValidMove(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        if (checkValidKill(chessBoard, startX, startY, endX, endY)) {
            return true;
        }

        // 2. Nhập thành
        if (getMoveNumber() > 0) {
            return false;
        }

        int rank = (getTeam() == Team.WHITE) ? 8 : 1; // Hàng của vua tùy theo màu
        // Kiểm tra nếu đang đứng đúng vị trí vua ban đầu
        if (startY == rank && startX == 5 && endY == rank) {
            // Nhập thành Kingside (vua đi sang g7 -> x = 7)
            if (endX == 7) {
                // Kiểm tra xe đã di chuyển chưa
                if (!chessBoard.existChessPiece(8, rank)
                        || chessBoard.getChessPiece(8, rank).getMoveNumber() > 0) return false;

                if (chessBoard.existChessPiece(6, rank) || chessBoard.existChessPiece(7, rank)) return false;
                if (chessBoard.isUnderAttack(5, rank, getTeam()) ||
                        chessBoard.isUnderAttack(6, rank, getTeam()) ||
                        chessBoard.isUnderAttack(7, rank, getTeam())) return false;
                return true;
            }

            // Nhập thành Queenside (vua đi sang c1/c8 -> x = 3)
            if (endX == 3) {
                // Kiểm tra xe đã di chuyển chưa
                if (!chessBoard.existChessPiece(1, rank)
                        || chessBoard.getChessPiece(1, rank).getMoveNumber() > 0) return false;

                if (chessBoard.existChessPiece(2, rank) ||
                        chessBoard.existChessPiece(3, rank) ||
                        chessBoard.existChessPiece(4, rank)) return false;
                if (chessBoard.isUnderAttack(5, rank, getTeam()) ||
                        chessBoard.isUnderAttack(4, rank, getTeam()) ||
                        chessBoard.isUnderAttack(3, rank, getTeam())) return false;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean checkValidKill(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        // The rook moves only in a straight line (either horizontally or vertically)
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);

        // The king moves only one square in any direction
        return dx <= 1 && dy <= 1;
    }
}