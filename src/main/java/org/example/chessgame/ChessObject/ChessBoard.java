package org.example.chessgame.ChessObject;

import org.example.chessgame.ChessObject.Move.Move;
import org.example.chessgame.ChessObject.Move.PreMove;
import org.example.chessgame.ChessObject.Move.SpecialMove;

import java.util.*;

public class ChessBoard {
    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private final ChessPiece[][] chessPieceBoard;
    private final int[][] kingPosition;
    private final Stack<Move> history;
    private List<PreMove> preMoveList;
    private String initFen;

    public ChessBoard() {
        //Start from 1 -> 8
        chessPieceBoard = new ChessPiece[9][9];
        kingPosition = new int[2][2];
        history = new Stack<>();
        preMoveList = new LinkedList<>();
    }

    public String getInitFen() {
        return initFen;
    }

    /**
     *
     * @param fen
     * @return current turn
     */
    public ChessPiece.Team loadFromFEN(String fen) {
        this.initFen = fen;

        for (int i = 1; i <= 8; i++) {
            Arrays.fill(chessPieceBoard[i], null);
        }

        history.clear();

        String[] parts = fen.split(" ");
        if (parts.length < 6) throw new IllegalArgumentException("Chuỗi FEN không hợp lệ");

        // === 1. Load bàn cờ ===
        String[] rows = parts[0].split("/");

        // === 2. Lượt đi ===
        ChessPiece.Team currentTurn = parts[1].equals("w") ? ChessPiece.Team.WHITE : ChessPiece.Team.BLACK;

        // === 3. Nhập thành ===
        String castlingRights = parts[2];

        // === 4. En passant ===
        String enPassantTarget = parts[3];

        // === 5. Halfmove clock ===
        int halfMoveClock = Integer.parseInt(parts[4]);

        // === 6. Fullmove number ===
        int fullMoveNumber = Integer.parseInt(parts[5]);

        boolean whiteKingSide = castlingRights.contains("K");
        boolean whiteQueenSide = castlingRights.contains("Q");
        boolean blackKingSide = castlingRights.contains("k");
        boolean blackQueenSide = castlingRights.contains("q");

        // === 1. Load bàn cờ ===
        for (int row = 0; row < 8; row++) {
            String fenRow = rows[row];
            int col = 0;

            for (char ch : fenRow.toCharArray()) {
                if (Character.isDigit(ch)) {
                    col += ch - '0'; // bỏ qua ô trống
                } else {
                    int x = col + 1;        // cột (1..8)
                    int y = row + 1;        // hàng (8..1)

                    ChessPiece.Team team = Character.isUpperCase(ch) ? ChessPiece.Team.WHITE : ChessPiece.Team.BLACK;
                    ch = Character.toLowerCase(ch);

                    ChessPiece piece = switch (ch) {
                        case 'p' -> new Pawn(team);
                        case 'r' -> new Rook(team);
                        case 'n' -> new Knight(team);
                        case 'b' -> new Bishop(team);
                        case 'q' -> new Queen(team);
                        case 'k' -> new King(team);
                        default -> null;
                    };

                    if (piece != null) {
                        if (piece instanceof King) {
                            if (team == ChessPiece.Team.WHITE && (x != 5 || y != 8)) {
                                piece.changeMoveNumber(1);
                            }
                            if (team == ChessPiece.Team.BLACK && (x != 5 || y != 1)) {
                                piece.changeMoveNumber(1);
                            }
                        } else if (piece instanceof Rook) {
                            if (team == ChessPiece.Team.WHITE) {
                                // Rook trắng king-side (h8)
                                if (x == 8 && y == 8) {
                                    piece.changeMoveNumber(whiteKingSide ? 0 : 1);
                                }
                                // Rook trắng queen-side (a8)
                                else if (x == 1 && y == 8) {
                                    piece.changeMoveNumber(whiteQueenSide ? 0 : 1);
                                }
                            } else {
                                // Rook đen king-side (h1)
                                if (x == 8 && y == 1) {
                                    piece.changeMoveNumber(blackKingSide ? 0 : 1);
                                }
                                // Rook đen queen-side (a1)
                                else if (x == 1 && y == 1) {
                                    piece.changeMoveNumber(blackQueenSide ? 0 : 1);
                                }
                            }
                        }
                        setChessPiece(x, y, piece);
                    }

                    col++;
                }
            }
        }

        return currentTurn;
    }

    /**
     *
     * @param fen
     * @return current turn
     */
    public ChessPiece.Team initChessBoard(String fen) {
        return loadFromFEN(fen);
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

    public static boolean isOutside(int x, int y) {
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
        moveChess(endX, endY, 0, 0, getLastMove(0).isPreMove);
        setChessPiece(endX, endY, chessPiece);
        getLastMove(0).specialMove = SpecialMove.PROMOTION;
    }

    /**
     * not check and just move chess piece
     */
    private void moveChess(int startX, int startY, int endX, int endY, boolean isPreMove) {
        history.add(new Move(startX, startY, endX, endY, getChessPiece(endX, endY), SpecialMove.NORMAL, isPreMove));

        setChessPiece(endX, endY, getChessPiece(startX, startY));
        setChessPiece(startX, startY, null);

        getChessPiece(endX, endY).changeMoveNumber(1);
    }

    public Move getLastMove(int lastNumber) {
        if (history.size() <= lastNumber) {
            return null;
        }
        return history.get(history.size() - 1 - lastNumber);
    }

    public PreMove getLastPreMove(int lastNumber) {
        if (preMoveList.isEmpty()) {
            return null;
        }
        return preMoveList.get(preMoveList.size() - 1 - lastNumber);
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

        if (lastMove.specialMove != SpecialMove.NORMAL) {
            moves.add(rollbackOne());
        }

        return moves;
    }

    public List<Move> rollbackAllPreMoves() {
        List<Move> moves = new ArrayList<>();

        while (!history.isEmpty() && history.getLast().isPreMove) {
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

    public boolean checkKingInCheck(ChessPiece.Team team) {
        int kingX = kingPosition[team.ordinal()][0];
        int kingY = kingPosition[team.ordinal()][1];
        return isUnderAttack(kingX, kingY, team);
    }

    public boolean checkCanPreMovePiece(int startX, int startY, int endX, int endY) {
        return getChessPiece(startX, startY).checkCanPreMove(this, startX, startY, endX, endY);
    }

    public boolean checkCanMovePiece(int startX, int startY, int endX, int endY) {
        if (existChessPiece(endX, endY)
                && getChessPieceTeam(startX, startY) == getChessPieceTeam(endX, endY)) {
            return false;
        }

        ChessPiece.Team team = getChessPieceTeam(startX, startY);
        if (getChessPiece(startX, startY).checkValidMove(this, startX, startY, endX, endY)) {
            // Simulation
            moveChess(startX, startY, endX, endY, false);
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
     * if don't call continuePreMove in GameController, this will clear PreMoveList
     */
    public List<PreMove> continueInPreMoveList() {
        List<PreMove> moves = preMoveList;
        preMoveList = new LinkedList<>();

        return moves;
    }

    /**
     * call checkCanPreMovePiece() before call this func if isPreMove=true
     * call checkCanMovePiece() before call this func if isPreMove=false
     */
    public void moveChessPiece(int startX, int startY, int endX, int endY, boolean isPreMove) {
        if (isPreMove) {
            preMoveList.add(new PreMove(startX, startY, endX, endY));
        }

        SpecialMove specialMove = SpecialMove.NORMAL;
        // Kiểm tra nhập thành
        if (getChessPiece(startX, startY) instanceof King && Math.abs(endX - startX) == 2) {
            // Di chuyển xe
            if (endX == 3) {
                moveChess(1, startY, startX - 1, endY, isPreMove);
            } else {
                moveChess(8, startY, startX + 1, endY, isPreMove);
            }
            specialMove = SpecialMove.CASTLE;
        }
        // Kiểm tra bắt tốt qua đường
        int direction = (getChessPieceTeam(startX, startY) == ChessPiece.Team.WHITE) ? -1 : 1; // White moves up (-1), Black moves down (+1)
        if (getChessPiece(startX, startY) instanceof Pawn && Math.abs(startX - endX) == 1 && startY + direction == endY && !existChessPiece(endX, endY)) {
            if (existChessPiece(endX, endY - direction) && getChessPieceTeam(endX, endY - direction) != getChessPieceTeam(startX, startY)) {
                moveChess(endX, endY - direction, 0, 0, isPreMove);
                specialMove = SpecialMove.EN_PASSANT;
            }
        }

        moveChess(startX, startY, endX, endY, isPreMove);

        if (specialMove != SpecialMove.NORMAL) {
            // Đảo để đảm bảo move event nằm sau extra move
            Move moveEvent = history.pop();
            Move extraMove = history.pop();
            history.push(moveEvent);
            history.push(extraMove);

            getLastMove(0).specialMove = specialMove;
        }
    }
}
