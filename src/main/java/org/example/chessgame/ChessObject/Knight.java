package org.example.chessgame.ChessObject;

public class Knight extends ChessPiece {
    public Knight(Team team) {
        super(team);
        createChessImage("knight");
    }

    public int getSkinCount() {
        return 6;
    }

    @Override
    public boolean checkValidKill(ChessBoard chessBoard, int startX, int startY, int endX, int endY) {
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);

        // Quân mã chỉ di chuyển theo hình chữ "L"
        return dx == 2 && dy == 1 || dx == 1 && dy == 2;
    }
}