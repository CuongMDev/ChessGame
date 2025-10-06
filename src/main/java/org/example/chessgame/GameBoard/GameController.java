package org.example.chessgame.GameBoard;

import javafx.animation.TranslateTransition;
import org.example.chessgame.Utils.ColorHighlighter;
import org.example.chessgame.Utils.Utils;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.example.chessgame.Abstract.Controller;
import org.example.chessgame.ChessObject.*;
import org.example.chessgame.ChessObject.Move.Move;
import org.example.chessgame.ChessObject.Move.PreMove;
import org.example.chessgame.ChessObject.Move.SpecialMove;
import org.example.chessgame.Socket.GameSocket;
import org.example.chessgame.Socket.SocketListener;
import org.example.chessgame.Sound.GameSounds;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GameController extends Controller {
    @FXML
    private GridPane chessBoardBox;
    private Pane[][] chessBoardPane;
    @FXML
    private StackPane mainStackPane;
    @FXML
    private HBox gameBox;
    @FXML
    private Pane overlayPane;
    private ChessPiece.Team playerTurn;
    private ChessPiece.Team currentTurn;
    private ChessPiece.Team teamPerspective;

    public GameSounds gameSound;

    private ChooseTeamController chooseTeamController;
    public GameResultController gameResultController;

    @FXML
    Button drawButton;
    @FXML
    Button resignButton;
    @FXML
    Button rollbackButton;

    private boolean gameOver;
    private boolean playWithBot;
    private String save_result;

    // move transition
    private Stack<ChessTransition> saveTransition;

    // promotion
    boolean isPromoting;
    List<Pane> promotionChessPanes;

    ChessBoard chessBoard;
    ColorHighlighter highlighter;

    ImageView saveChessImage;

    private GameSocket gameSocket;

    private String current_moveUCI;

    //event
    AtomicReference<int[]> startCell = new AtomicReference<>();
    AtomicReference<Pane> containPane = new AtomicReference<>();
    AtomicReference<int[]> currentCell = new AtomicReference<>();
    AtomicInteger mouseStage = new AtomicInteger(); // 0: chưa chọn, 1: đang kéo
    AtomicBoolean needToCancel = new AtomicBoolean();
    ImageView currentChooseImage;
    ImageView currentCopyChooseImage;

    @FXML
    private void onRollbackClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            Platform.runLater(this::rollback);
        }
    }

    private void rollback() {
        if (currentChooseImage != null) {
            currentChooseImage.fireEvent(Utils.SecondaryMouse);
        }

        rollbackAndClearAllPreMoves();
        drawButton.setDisable(true);
        do {
            highlightLastMoveEvent(true);
            List<Move> moveList = chessBoard.rollback();

            stopAllTransitions();
            for (Move move : moveList) {
                moveChessPane(move.endX, move.endY, move.startX, move.startY, false);
                if (move.deadPiece != null) {
                    if (move.endX == 0 && move.endY == 0) {
                        saveChessImage = move.deadPiece.getChessImage();
                    } else {
                        getPaneFromGridPane(move.endX, move.endY).getChildren().setAll(move.deadPiece.getChessImage());
                    }
                }
            }
            changeTurn();

            // play with bot => size > 1
            // play with player => size > 0
            if (chessBoard.getLastMove(playWithBot ? 1 : 0) == null) {
                rollbackButton.setDisable(true);
            } else {
                rollbackButton.setDisable(false);
            }

            if (isPromoting) {
                refresh();
                isPromoting = false;
            } else {
                gameSocket.sendRollbackData(); // Nếu ở trạng thái đang phong có nghĩa là move chưa được gửi => không cần gửi rollback
            }
            highlightLastMoveEvent(false);
        } while (currentTurn != playerTurn);
    }

    private void rollbackAllPreMoves() {
        List<Move> moveList = chessBoard.rollbackAllPreMoves();
        stopAllTransitions();
        for (Move move : moveList) {
            moveChessPane(move.endX, move.endY, move.startX, move.startY, false);
            if (move.specialMove == SpecialMove.NORMAL) { // is move event
                highlighter.popColorLast(getPaneFromGridPane(move.endX, move.endY), move.endX, move.endY);
                highlighter.popColorLast(getPaneFromGridPane(move.startX, move.startY), move.startX, move.startY);
            }
            if (move.deadPiece != null) {
                if (move.endX == 0 && move.endY == 0) {
                    saveChessImage = move.deadPiece.getChessImage();
                } else {
                    getPaneFromGridPane(move.endX, move.endY).getChildren().setAll(move.deadPiece.getChessImage());
                }
            }
        }
        if (!moveList.isEmpty() && isPromoting) { // is promoting and not premove
            refresh();
            isPromoting = false;
        }
    }

    private void rollbackAndClearAllPreMoves() {
        rollbackAllPreMoves();
        chessBoard.continueInPreMoveList();
    }

    @FXML
    private void onDrawClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) {
            return;
        }

        onGameOver("Draw");
    }

    @FXML
    private void onResignClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) {
            return;
        }

        if (playerTurn == ChessPiece.Team.WHITE) {
            onGameOver("Black-Win");
        } else {
            onGameOver("White-Win");
        }
    }

    private int[] getCell(double posX, double posY) {
        Pane pane = getPaneFromGridPane(1, 1);
        double cellWidth = pane.getWidth();
        double cellHeight = pane.getHeight();

        int x = (int)(posX / cellWidth);
        int y = (int)(posY / cellHeight);
        if (teamPerspective == ChessPiece.Team.BLACK) {
            x = 9 - x;
            y = 9 - y;
        }
        return new int[]{x, y};
    }

    private boolean playCell(int startX, int startY, int endX, int endY, ChessPiece promotionPiece, boolean[] useChessTransition) {
        if (ChessBoard.isOutside(endX, endY)) {
            return false;
        }

        boolean isPreMove = chessBoard.getChessPieceTeam(startX, startY) != currentTurn;
        if (handleMoveEvent(startX, startY, endX, endY, promotionPiece, useChessTransition)) {
            current_moveUCI = Character.toString(startX - 1 + 'a') + (9 - startY) + (char) (endX - 1 + 'a') + (9 - endY);
            if (!isPromoting && !isPreMove) {
                if (promotionPiece != null) {
                    switch (promotionPiece) {
                        case Queen _ -> current_moveUCI += 'q';
                        case Rook _ -> current_moveUCI += 'r';
                        case Knight _ -> current_moveUCI += 'n';
                        case Bishop _ -> current_moveUCI += 'b';
                        default -> throw new IllegalStateException("Unexpected value: " + promotionPiece);
                    }
                }
                gameSocket.sendMoveData(current_moveUCI);
            }
            if (isPreMove && promotionPiece != null) {
                chessBoard.getLastPreMove(0).promotedPiece = promotionPiece;
            }

            return true;
        }

        return false;
    }

    private boolean emitPlayerEvent(int startX, int startY, int endX, int endY, boolean useExtraChessTransition) {
        boolean isPreMove = chessBoard.getChessPieceTeam(startX, startY) != currentTurn;
        if (playCell(startX, startY, endX, endY, null, new boolean[]{useExtraChessTransition, true})) {
            if (isPreMove) {
                gameSound.premoveSound.play();
            }
            return true;
        }

        return false;
    }

    private void emitBotEvent(String moveUCI, boolean canDraw, String result) {
        if (gameOver) {
            return;
        }
        if (moveUCI != null) {
            rollbackAllPreMoves();

            int startX = moveUCI.charAt(0) - 'a' + 1;
            int startY = 9 - Integer.parseInt(String.valueOf(moveUCI.charAt(1)));
            int endX = moveUCI.charAt(2) - 'a' + 1;
            int endY = 9 - Integer.parseInt(String.valueOf(moveUCI.charAt(3)));
            ChessPiece promotionPiece = null;
            if (moveUCI.length() > 4) {
                char promotionChar = moveUCI.charAt(4);
                ChessPiece.Team team = chessBoard.getChessPieceTeam(startX, startY);
                promotionPiece = switch (promotionChar) {
                    case 'q' -> new Queen(team);
                    case 'r' -> new Rook(team);
                    case 'n' -> new Knight(team);
                    case 'b' -> new Bishop(team);
                    default -> promotionPiece;
                };
            }

            if (promotionPiece != null) {
                addChessPieceEvents(promotionPiece.getChessImage());
            }
            handleMoveEvent(startX, startY, endX, endY, promotionPiece, new boolean[]{true, true});
        }
        
        drawButton.setDisable(playerTurn != currentTurn || !canDraw);

        switch (result) {
            case "1/2-1/2" -> onGameOver("Draw");
            case "1-0" -> onGameOver("White-Win");
            case "0-1" -> onGameOver("Black-Win");
            default -> continuePreMove();
        }
    }

    private Pane getPaneFromGridPane(int column, int row) {
        return teamPerspective == ChessPiece.Team.WHITE ? chessBoardPane[row][column] : chessBoardPane[9 - row][9 - column];
    }

    private Pane getPaneFromGridPane(ImageView imageView) {
        for (Node node : chessBoardBox.getChildren()) {
            Pane getPane = (Pane) node;
            if (getPane.getChildren().contains(imageView)) {
                return getPane;
            }
        }
        return null; // Trả về null nếu không tìm thấy
    }

    private boolean checkValidTurn(int x, int y) {
        ChessPiece.Team chessTeam = chessBoard.getChessPieceTeam(x, y);
        if (playWithBot) {
            return chessTeam == playerTurn; // có thể pre move
        }
        return chessTeam == currentTurn;
    }

    private void moveChessPane(int startX, int startY, int endX, int endY, boolean useChessTransition) {
        Pane startPane = getPaneFromGridPane(startX, startY);
        if (startX == 0 && startY == 0) {
            startPane.getChildren().add(saveChessImage);
        }
        if (endX == 0 && endY == 0) {
            saveChessImage = (ImageView) startPane.getChildren().removeLast();
            return;
        }

        Pane endPane = getPaneFromGridPane(endX, endY);

        ImageView chessImage = (ImageView) startPane.getChildren().removeLast();
        endPane.getChildren().setAll(chessImage);

        if (useChessTransition) {
            addChessTransition(chessImage, startPane, endPane);
        }
    }

    public void addChessTransition(ImageView chessImage, Pane startPane, Pane endPane) {
        ImageView copyChessImage = Utils.copyImageView(endPane, chessImage);

        double imageX = copyChessImage.getLayoutX();
        double imageY = copyChessImage.getLayoutY();
        Point2D startPointInOverlayPane = Utils.sceneToScene(imageX, imageY, startPane, overlayPane);
        Point2D endPointInOverlayPane = Utils.sceneToScene(imageX, imageY, endPane, overlayPane);

        // Thêm vào overlayPane
        overlayPane.getChildren().add(copyChessImage);
        copyChessImage.setTranslateX(startPointInOverlayPane.getX());
        copyChessImage.setTranslateY(startPointInOverlayPane.getY());
        chessImage.setOpacity(0);

        // create move animation
        ChessTransition chessTransition = new ChessTransition(copyChessImage, endPointInOverlayPane.getX(), endPointInOverlayPane.getY(), 100);
        chessTransition.setOnFinished(() -> {
            saveTransition.remove(chessTransition);
            overlayPane.getChildren().remove(copyChessImage);
            copyChessImage.setTranslateX(0);
            copyChessImage.setTranslateY(0);
            chessImage.setOpacity(1);
        });

        saveTransition.add(chessTransition);
        chessTransition.start();
    }

    private void stopAllTransitions() {
        while (!saveTransition.isEmpty()) {
            ChessTransition lastTransition = saveTransition.removeLast();
            lastTransition.stop();
        }
    }

    public void continuePreMove() {
        List<PreMove> moves = chessBoard.continueInPreMoveList();
        for (PreMove move : moves) {
            if ((!chessBoard.existChessPiece(move.startX, move.startY) /*en passant*/ || chessBoard.getChessPieceTeam(move.startX, move.startY) != playerTurn /*taken*/)
                || (!playCell(move.startX, move.startY, move.endX, move.endY, move.promotedPiece, new boolean[]{false, false}))) {

                if (currentChooseImage != null) {
                    currentChooseImage.fireEvent(Utils.SecondaryMouse); // Thả
                }
                rollbackAllPreMoves();
                break;
            }
        }
    }

    private void onGameOver(String result) {
        gameSocket.sendCancelSearching();

        save_result = result;
        gameOver = true;
        rollbackAndClearAllPreMoves();
        if (isPromoting) {
            isPromoting = false;
            refresh();
        }
        rollbackButton.setDisable(true);
        resignButton.setDisable(true);
        if (currentChooseImage != null) {
            currentChooseImage.fireEvent(Utils.SecondaryMouse); // Thả
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            gameResultController.setResult(result);
            mainStackPane.getChildren().add(gameResultController.getParent());
            gameSound.gameEndSound.play();
        });
        pause.play();
    }

    private void createPromotionBoard(int endX, int endY, boolean isPreMove) {
        ChessPiece.Team team = chessBoard.getChessPieceTeam(endX, endY); // Lấy team
        promotionChessPanes = Arrays.asList(getPaneFromGridPane(endX, endY - 1), getPaneFromGridPane(endX + 1, endY),
                getPaneFromGridPane(endX, endY + 1), getPaneFromGridPane(endX - 1, endY));
        if (team == ChessPiece.Team.BLACK) {
            Collections.swap(promotionChessPanes, 0, 2);
            Collections.swap(promotionChessPanes, 1, 3);
        }

        ChessPiece[] promotionPieces = new ChessPiece[]{new Queen(team), new Rook(team), new Knight(team), new Bishop(team)};
        char[] promotionChars = new char[]{'q', 'r', 'n', 'b'};

        // Thêm 4 quân phong
        for (int i = 0; i < 4; i++) {
            // Phần pane trắng
            Pane promotionPane = new Pane();
            promotionPane.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 3, 3);"
            );

            promotionPane.prefWidthProperty().bind(promotionChessPanes.get(i).widthProperty());
            promotionPane.prefHeightProperty().bind(promotionChessPanes.get(i).heightProperty());

            ChessPiece promotionPiece = promotionPieces[i];
            char promotionChar = promotionChars[i];
            ImageView promotionChessImage = promotionPiece.getChessImage();
            Utils.setImageToCell(promotionPane, promotionChessImage);
            promotionChessPanes.get(i).getChildren().add(promotionPane);

            // Các events chuột
            // Hover vào ảnh -> hiện bàn tay
            promotionChessImage.setOnMouseEntered(_ -> promotionChessImage.setCursor(Cursor.HAND));
            // Rời chuột khỏi ảnh -> về mặc định
            promotionChessImage.setOnMouseExited(_ -> promotionChessImage.setCursor(Cursor.DEFAULT));

            // Thêm sự kiện phong
            promotionChessImage.setOnMousePressed(event -> {
                // Kiểm tra chuột trái
                if (event.getButton() != MouseButton.PRIMARY) {
                    return;
                }

                setPromotionPiece(endX, endY, promotionPiece);
                addChessPieceEvents(promotionChessImage);

                if (isPreMove) {
                    chessBoard.getLastPreMove(0).promotedPiece = promotionPiece;
                } else {
                    // Gửi move
                    current_moveUCI += promotionChar;
                    gameSocket.sendMoveData(current_moveUCI);
                }

                removePromotionBoard();
            });
        }
    }

    private void setPromotionPiece(int x, int y, ChessPiece promotionPiece) {
        ImageView promotionChessImage = promotionPiece.getChessImage();

        moveChessPane(x, y, 0, 0, false); // Save
        if (promotionChessImage == currentChooseImage) {
            Utils.setImageToCell(getPaneFromGridPane(x, y), currentCopyChooseImage);
        } else {
            Utils.setImageToCell(getPaneFromGridPane(x, y), promotionChessImage);
        }
        chessBoard.setPromote(x, y, promotionPiece);

        gameSound.promotionSound.play();

        isPromoting = false;
    }

    // highlight yellow
    private void highlightLastMoveEvent(boolean unhighlight) {
        Move lastMove = chessBoard.getLastMove(0);
        if (lastMove == null) {
            return;
        }
        if (lastMove.specialMove != SpecialMove.NORMAL) {
            lastMove = chessBoard.getLastMove(1); // đổi sang move event vì đang ở extra move
        }

        if (unhighlight) {
            highlighter.popColorLast(getPaneFromGridPane(lastMove.startX, lastMove.startY), lastMove.startX, lastMove.startY);
            highlighter.popColorLast(getPaneFromGridPane(lastMove.endX, lastMove.endY), lastMove.endX, lastMove.endY);
        } else {
            highlighter.addColorLast(getPaneFromGridPane(lastMove.startX, lastMove.startY), lastMove.startX, lastMove.startY, Color.YELLOW);
            highlighter.addColorLast(getPaneFromGridPane(lastMove.endX, lastMove.endY), lastMove.endX, lastMove.endY, Color.YELLOW);
        }
    }

    private boolean checkCanMovePiece(int startX, int startY, int endX, int endY) {
        boolean isPreMove = chessBoard.getChessPieceTeam(startX, startY) != currentTurn;

        // Check valid move
        return  ((!isPreMove && chessBoard.checkCanMovePiece(startX, startY, endX, endY)) ||
                (isPreMove && chessBoard.checkCanPreMovePiece(startX, startY, endX, endY)));
    }

    /**
     * useChessTransition has for event move and extra move
     * @return if move is valid
     */
    private boolean handleMoveEvent(int startX, int startY, int endX, int endY, ChessPiece promotionPiece, boolean[] useChessTransition) {
        // Kiểm tra nếu 2 ô trùng nhau
        if (startX == endX && startY == endY) {
            return false;
        }

        boolean isPreMove = chessBoard.getChessPieceTeam(startX, startY) != currentTurn;

        if (checkCanMovePiece(startX, startY, endX, endY)) {
            if (isPreMove) {
                highlighter.addColorLast(getPaneFromGridPane(startX, startY), startX, startY, Color.RED);
                highlighter.addColorLast(getPaneFromGridPane(endX, endY), endX, endY, Color.RED);
            } else {
                // Highlight yellow
                highlightLastMoveEvent(true);
            }

            stopAllTransitions();
            chessBoard.moveChessPiece(startX, startY, endX, endY, isPreMove);
            // Phong cấp sẽ di chuyển kiểu khác
            moveChessPane(startX, startY, endX, endY, useChessTransition[0] && promotionPiece == null);
            if (!isPreMove) {
                changeTurn();
            }

            // Đặc biệt thì di chuyển thêm lần nữa
            if (chessBoard.getLastMove(0).specialMove != SpecialMove.NORMAL) {
                Move extraMove = chessBoard.getLastMove(0);
                moveChessPane(extraMove.startX, extraMove.startY, extraMove.endX, extraMove.endY, useChessTransition[1]);
            }

            if (chessBoard.checkPromotion(endX, endY)) {
                if (promotionPiece == null) {
                    isPromoting = true;
                    createPromotionBoard(endX, endY, isPreMove);
                } else {
                    setPromotionPiece(endX, endY, promotionPiece);
                    if (useChessTransition[0]) {
                        addChessTransition(promotionPiece.getChessImage(), getPaneFromGridPane(startX, startY), getPaneFromGridPane(endX, endY));
                    }
                }
            }

            if (!isPreMove) {
                highlightLastMoveEvent(false);

                // play sound
                if (chessBoard.checkKingInCheck(currentTurn)) {
                    gameSound.moveCheckSound.play();
                } else if (!isPromoting) {
                    SpecialMove specialMove = chessBoard.getLastMove(0).specialMove;
                    if (specialMove == SpecialMove.NORMAL) {
                        if (chessBoard.getLastMove(0).deadPiece == null) {
                            gameSound.moveSelfSound.play();
                        } else {
                            gameSound.captureSound.play();
                        }
                    } else if (specialMove == SpecialMove.CASTLE) {
                        gameSound.castleSound.play();
                    } else if (specialMove == SpecialMove.EN_PASSANT) {
                        gameSound.captureSound.play();
                    }
                }

                // play with bot => size > 1
                // play with player => size > 0
                if (chessBoard.getLastMove((playWithBot && playerTurn == ChessPiece.Team.BLACK) ? 1 : 0) == null) {
                    rollbackButton.setDisable(true);
                } else {
                    rollbackButton.setDisable(false);
                }
            }

            return true;
        }

        return false;
    }

    void changeTurn() {
        if (currentTurn == ChessPiece.Team.BLACK) {
            currentTurn = ChessPiece.Team.WHITE;
        } else {
            currentTurn = ChessPiece.Team.BLACK;
        }

        if (!playWithBot) {
            if (playerTurn == ChessPiece.Team.BLACK) {
                playerTurn = ChessPiece.Team.WHITE;
            } else {
                playerTurn = ChessPiece.Team.BLACK;
            }
        }
    }

    private void removePromotionBoard() {
        // Bỏ phần chọn phong
        if (promotionChessPanes != null) {
            for (Pane chessPane : promotionChessPanes) {
                chessPane.getChildren().removeLast();
            }
            promotionChessPanes = null;
        }
    }

    private void cancelPlaying(ImageView originalImage, int type) {
        if (type == 0) {
            originalImage.setTranslateX(0);
            originalImage.setTranslateY(0);

            overlayPane.getChildren().remove(originalImage); // Xóa khỏi overlayPane

            if (containPane.get().getChildren().isEmpty() || containPane.get().getChildren().getFirst() != currentCopyChooseImage) {
                Pane findContainPane = getPaneFromGridPane(currentCopyChooseImage);
                if (findContainPane != null) {
                    findContainPane.getChildren().setAll(originalImage);
                }
            } else {
                containPane.get().getChildren().setAll(originalImage); // Đổi lại image
            }

            if (!ChessBoard.isOutside(currentCell.get()[0], currentCell.get()[1])) {
                getPaneFromGridPane(currentCell.get()[0], currentCell.get()[1]).getStyleClass().remove("highlight-border"); // Xoá viền
            }

            currentCopyChooseImage = null;

            mouseStage.set(0);
        } else {
            highlighter.popColorFirst(containPane.get(), startCell.get()[0], startCell.get()[1]);

            currentChooseImage = null;
        }
    }

    private void addChessPieceEvents(ImageView image) {
        // Tạo event kéo thả
        // Sự kiện nhấn chuột để lưu vị trí bắt đầu

        // Hover vào ảnh -> hiện bàn tay mở
        image.setOnMouseEntered(_ -> image.setCursor(Cursor.OPEN_HAND));
        // Rời chuột khỏi ảnh -> về mặc định
        image.setOnMouseExited(_ -> image.setCursor(Cursor.DEFAULT));

        // Sự kiện bấm chuột
        image.setOnMousePressed(event -> {
            // Sự kiện chọn
            if (event.getButton() == MouseButton.PRIMARY) {
                if (isPromoting || gameOver) {
                    return;
                }
                if (mouseStage.get() != 0) { // Không đúng stage
                    return;
                }

                Point2D localInOverlayPane = overlayPane.sceneToLocal(event.getSceneX(), event.getSceneY());

                int[] newStartCell = getCell(localInOverlayPane.getX(), localInOverlayPane.getY());
                Pane newContainPane = getPaneFromGridPane(image);
                // Sửa lỗi ô không khớp vị trí
                if (!chessBoard.existChessPiece(newStartCell[0], newStartCell[1]) || image != chessBoard.getChessPiece(newStartCell[0], newStartCell[1]).getChessImage()) { // Không trùng ảnh đang chọn
                    return;
                }

                // Di chuyển hợp lệ thì kích hoạt sự kiện cell
                if (currentChooseImage != null && checkCanMovePiece(startCell.get()[0], startCell.get()[1], newStartCell[0], newStartCell[1])) {
                    newContainPane.fireEvent(event);
                    return;
                }
                // Không đúng lượt thì bỏ qua
                if (!checkValidTurn(newStartCell[0], newStartCell[1])) {
                    return;
                }

                if (currentChooseImage != image) {
                    needToCancel.set(false);
                }
                // Xoá màu ô trước trước khi tô màu ô mới
                if (currentChooseImage != null) {
                    cancelPlaying(currentChooseImage, 1);
                }
                // Lưu điểm bắt đầu
                startCell.set(newStartCell);
                currentCell.set(newStartCell);
                containPane.set(newContainPane);

                ImageView copyImage = Utils.copyImageView(containPane.get(), image);
                copyImage.setOpacity(0.3); // Làm mờ
                containPane.get().getChildren().setAll(copyImage); // Đổi sang copy image

                // Khi nhấn giữ chuột -> bàn tay đóng
                image.setCursor(Cursor.CLOSED_HAND);

                currentChooseImage = image;
                currentCopyChooseImage = copyImage;

                overlayPane.getChildren().add(image); // Thêm vào overlayPane

                highlighter.addColorFirst(containPane.get(), startCell.get()[0], startCell.get()[1], Color.YELLOW);

                if (!ChessBoard.isOutside(currentCell.get()[0], currentCell.get()[1])) {
                    getPaneFromGridPane(currentCell.get()[0], currentCell.get()[1]).getStyleClass().add("highlight-border"); // Thêm viền
                }

                image.setTranslateX(localInOverlayPane.getX() - image.getFitWidth() / 2);
                image.setTranslateY(localInOverlayPane.getY() - image.getFitHeight() / 2);

                mouseStage.set(1);
            }

            // Sự kiện huỷ
            if (event.getButton() == MouseButton.SECONDARY) {
                if (currentCopyChooseImage != null) {
                    cancelPlaying(image, 0);
                }
                if (currentChooseImage != null) {
                    cancelPlaying(image, 1);
                }
            }
        });

        // Sự kiện kéo
        image.setOnMouseDragged(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            if (mouseStage.get() != 1) { // Không đúng stage
                return;
            }

            Point2D localInOverlayPane = overlayPane.sceneToLocal(event.getSceneX(), event.getSceneY());
            image.setTranslateX(localInOverlayPane.getX() - image.getFitWidth() / 2);
            image.setTranslateY(localInOverlayPane.getY() - image.getFitHeight() / 2);

            int[] curCell = getCell(localInOverlayPane.getX(), localInOverlayPane.getY());
            if (!Arrays.equals(currentCell.get(), curCell)) {
                if (!ChessBoard.isOutside(currentCell.get()[0], currentCell.get()[1])) {
                    getPaneFromGridPane(currentCell.get()[0], currentCell.get()[1]).getStyleClass().remove("highlight-border"); // Xoá viền pane cũ
                }
                if (!ChessBoard.isOutside(curCell[0], curCell[1])) {
                    getPaneFromGridPane(curCell[0], curCell[1]).getStyleClass().add("highlight-border"); // Thêm viền
                }
                currentCell.set(curCell);
            }
        });

        // Sự kiện thả
        image.setOnMouseReleased(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            if (mouseStage.get() != 1) { // Không đúng stage
                return;
            }

            Point2D localInOverlayPane = overlayPane.sceneToLocal(event.getSceneX(), event.getSceneY());
            int[] endCell = getCell(localInOverlayPane.getX(), localInOverlayPane.getY());

            cancelPlaying(image, 0);
            if (Arrays.equals(startCell.get(), endCell)) {
                if (needToCancel.get()) {
                    cancelPlaying(image, 1);
                    needToCancel.set(false);
                } else {
                    needToCancel.set(true);
                }
            }

            // Kích hoạt sự kiện
            if (emitPlayerEvent(startCell.get()[0], startCell.get()[1], endCell[0], endCell[1], false)) {
                cancelPlaying(image, 1);
                needToCancel.set(false);
            }
        });
    }

    private void addCellEvents(Pane pane) {
        // Sự kiện bấm chuột
        pane.setOnMousePressed(event -> {
            // Sự kiện chọn
            if (event.getButton() == MouseButton.PRIMARY) {
                if (mouseStage.get() != 0) { // Không đúng stage
                    return;
                }

                if (currentChooseImage != null) {
                    cancelPlaying(currentChooseImage, 1);

                    Point2D endPointInOverlayPane = overlayPane.sceneToLocal(event.getSceneX(), event.getSceneY());
                    int[] endCell = getCell(endPointInOverlayPane.getX(), endPointInOverlayPane.getY());
                    // Kích hoạt sự kiện
                    emitPlayerEvent(startCell.get()[0], startCell.get()[1], endCell[0], endCell[1], true);
                }
            }
        });
    }

    @Override
    public void refresh() {
        removePromotionBoard();

        for (Node node : chessBoardBox.getChildren()) {
            int x = GridPane.getColumnIndex(node);
            int y = GridPane.getRowIndex(node);
            if (x < 1 || x > 8 || y < 1 || y > 8) {
                continue;
            }

            Pane cell = (Pane) node;
            ChessPiece chessPiece = (teamPerspective == ChessPiece.Team.WHITE) ? chessBoard.getChessPiece(x, y) : chessBoard.getChessPiece(9 - x, 9 - y);
            if (chessPiece != null) {
                ImageView chessImage = chessPiece.getChessImage();

                Utils.setImageToCell(cell, chessImage);

                addChessPieceEvents(chessImage);
            } else {
                cell.getChildren().clear();
            }
        }
    }

    private void initChessBoard() {
        chessBoard = new ChessBoard();
        chessBoardPane = new Pane[10][10];
        saveTransition = new Stack<>();
        highlighter = new ColorHighlighter();
        overlayPane.setMouseTransparent(true);

        gameBox.setPadding(new Insets(2, 0, 2, 0)); // Giảm viền trên và dưới

        chessBoardBox.getColumnConstraints().clear();
        chessBoardBox.getRowConstraints().clear();
        chessBoardBox.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                rollbackAllPreMoves();
                chessBoard.continueInPreMoveList();
                if (currentChooseImage != null) {
                    currentChooseImage.fireEvent(Utils.SecondaryMouse);
                }
                if (isPromoting) {
                    Platform.runLater(this::rollback);
                }
            }
        });

        // Định nghĩa n hàng, cột
        for (int i = 0; i < 10; i++) {
            RowConstraints row = new RowConstraints();
            ColumnConstraints column = new ColumnConstraints();
            row.setPercentHeight(100.0 / 10);
            column.setPercentWidth(100.0 / 10);

            chessBoardBox.getRowConstraints().add(row);
            chessBoardBox.getColumnConstraints().add(column);
        }

        // Always square
        chessBoardBox.prefWidthProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.min(gameBox.getHeight(), gameBox.getWidth()),
                        gameBox.heightProperty(),
                        gameBox.widthProperty()
                )
        );
        chessBoardBox.prefHeightProperty().bind(chessBoardBox.prefWidthProperty());

        // Color chess board
        for (int row = 0; row <= 9; row++) {
            for (int col = 0; col <= 9; col++) {
                Pane cell = new Pane(); // Create new cell

                if (1 <= row && row <= 8 && 1 <= col && col <= 8) {
                    // if (row + col) even, color light
                    if ((row + col) % 2 == 0) {
                        cell.setStyle("-fx-background-color: #eeeed2;");
                    } else {
                        cell.setStyle("-fx-background-color: #769656;");
                    }

                    addCellEvents(cell);
                }

                chessBoardBox.add(cell, col, row);
                chessBoardPane[row][col] = cell;
            }
        }
    }

    public void resetGameBoard(boolean playWithBot, String fen) {
        while (mainStackPane.getChildren().size() > 1) {
            mainStackPane.getChildren().removeLast();
        }

        this.isPromoting = false;
        this.gameOver = false;
        drawButton.setDisable(true);
        resignButton.setDisable(false);
        rollbackButton.setDisable(true);
        saveTransition.clear();
        promotionChessPanes = null;

        highlightLastMoveEvent(true);
        this.currentTurn = chessBoard.initChessBoard(fen);

        if (playWithBot) {
            mainStackPane.getChildren().add(chooseTeamController.getParent());
            gameBox.setDisable(true);
        } else {
            resetGameplay(false, this.currentTurn, ChessPiece.Team.WHITE);
            gameBox.setDisable(false);
        }

        teamPerspective = ChessPiece.Team.WHITE;
        addNumOrder();
        refresh();
    }

    private void resetGameplay(boolean playWithBot, ChessPiece.Team humanTeam, ChessPiece.Team teamPerspective) {
        this.playWithBot = playWithBot;
        this.playerTurn = humanTeam;
        this.teamPerspective = teamPerspective;
        addNumOrder();
        if (teamPerspective == ChessPiece.Team.BLACK) {
            refresh();
        }

        gameSocket.sendResetData(playWithBot, !playWithBot || (humanTeam == this.currentTurn), chessBoard.getInitFen());
    }

    public void setThinkingAbility(double thinkingAbility, double searchThread) {
        gameSocket.sendChangeThinkingAbilityData(thinkingAbility, searchThread);
    }

    private void addNumOrder() {
        final String labelStyle = "-fx-font-size: 20px; -fx-font-weight: Bold; -fx-text-fill: #DDDDDD;";

        for (int row = 1; row <= 8; row++) {
            Label numLabel = new Label(String.valueOf((char) (teamPerspective == ChessPiece.Team.WHITE ? 'a' + row - 1 : 'a' + 8 - row)));
            numLabel.setStyle(labelStyle);
            VBox vbox = new VBox(numLabel);
            vbox.setAlignment(Pos.TOP_CENTER);

            Pane pane = chessBoardPane[9][row];

            vbox.prefHeightProperty().bind(pane.heightProperty());
            vbox.prefWidthProperty().bind(pane.widthProperty());

            pane.getChildren().setAll(vbox);
        }

        for (int col = 1; col <= 8; col++) {
            Label numLabel = new Label((teamPerspective == ChessPiece.Team.WHITE ? 9 - col : col) + " ");
            numLabel.setStyle(labelStyle);
            HBox vbox = new HBox(numLabel);
            vbox.setAlignment(Pos.CENTER_RIGHT);

            Pane pane = chessBoardPane[col][0];

            vbox.prefHeightProperty().bind(pane.heightProperty());
            vbox.prefWidthProperty().bind(pane.widthProperty());

            pane.getChildren().setAll(vbox);
        }
    }

    private void initAdditionController() throws IOException {
        chooseTeamController = (ChooseTeamController) Controller.init(getStage(), getClass().getResource("ChooseTeam/ChooseTeam.fxml"));
        chooseTeamController.whiteButton.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                resetGameplay(true, ChessPiece.Team.WHITE, ChessPiece.Team.WHITE);
                mainStackPane.getChildren().removeLast();
                gameBox.setDisable(false);
            }
        });
        chooseTeamController.blackButton.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                resetGameplay(true, ChessPiece.Team.BLACK, ChessPiece.Team.BLACK);
                mainStackPane.getChildren().removeLast();
                gameBox.setDisable(false);
            }
        });

        gameResultController = (GameResultController) Controller.init(getStage(), getClass().getResource("GameResult/GameResult.fxml"));

        // avoid lagging
        mainStackPane.getChildren().add(gameResultController.getParent());
        mainStackPane.getChildren().removeLast();

        gameResultController.rematchButton.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                mainStackPane.getChildren().removeLast();

                resetGameBoard(playWithBot, ChessBoard.STARTING_FEN);
            }
        });

        gameResultController.copyPGNButton.setOnMouseClicked(mouseEvent -> {
            gameSocket.sendRequirePGNData();
        });
    }

    public void initSocket() {
        gameSocket = new GameSocket();
        gameSocket.setListener(new SocketListener() {
            @Override
            public void onMoveReceived(String moveUCI, boolean canDraw, String result) {
                Platform.runLater(() -> emitBotEvent(moveUCI, canDraw, result));
            }

            @Override
            public void onPGNReceived(String pgn) {
                if (save_result.equals("Draw")) {
                    pgn = Utils.addResultToPGN(pgn, "1/2-1/2");
                } else if (save_result.equals("White-Win")) {
                    pgn = Utils.addResultToPGN(pgn, "1-0");
                } else {
                    pgn = Utils.addResultToPGN(pgn, "0-1");
                }
                Utils.copyToClipBoard(pgn);
            }

            @Override
            public void onError(String message) {
                System.out.println(message + "\n");
            }
        });

        gameSocket.startServer();
    }

    public void closeSocket() {
        try {
            gameSocket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSound() {
        gameSound = new GameSounds();
    }

    @FXML
    private void initialize() throws IOException {
        initSocket();
        initSound();
        initChessBoard();
        initAdditionController();
    }
}
