package org.example.chessgame.GameBoard;

import Utils.ColorHighlighter;
import Utils.Utils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.example.chessgame.Abstract.Controller;
import org.example.chessgame.ChessObject.*;
import org.example.chessgame.ChessObject.Move.Move;
import org.example.chessgame.ChessObject.Move.PreMove;
import org.example.chessgame.Socket.GameSocket;
import org.example.chessgame.Socket.SocketListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GameController extends Controller {
    @FXML
    GridPane chessBoardBox;
    @FXML
    HBox mainBox;
    @FXML
    Pane overlayPane;
    ChessPiece.Team playerTurn;
    ChessPiece.Team currentTurn;

    @FXML
    Button drawButton;

    @FXML
    Button rollbackButton;

    private boolean gameOver;
    private String result;
    private boolean playWithBot;

    // promotion
    boolean isPromoting;

    ChessBoard chessBoard;

    ImageView saveChessImage;

    private GameSocket gameSocket;

    private String current_moveUCI;
    ImageView currentChooseImage;
    ImageView currentCopyChooseImage;

    @FXML
    private void onRollbackClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            Platform.runLater(this::rollback);
        }
    }

    private void rollback() {
        rollbackAndClearAllPreMoves();
        drawButton.setDisable(true);
        do {
            highlightLastMoveEvent(true);
            List<Move> moveList = chessBoard.rollback();

            for (Move move : moveList) {
                moveChessPane(move.endX, move.endY, move.startX, move.startY);
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
        for (Move move : moveList) {
            moveChessPane(move.endX, move.endY, move.startX, move.startY);
            if (move.deadPiece != null) {
                if (move.endX == 0 && move.endY == 0) {
                    saveChessImage = move.deadPiece.getChessImage();
                } else {
                    getPaneFromGridPane(move.endX, move.endY).getChildren().setAll(move.deadPiece.getChessImage());
                }
            }
        }
        if (isPromoting) {
            refresh();
            isPromoting = false;
        }
    }

    private void rollbackAndClearAllPreMoves() {
        rollbackAllPreMoves();
        chessBoard.continueInPreMoveList();
        if (isPromoting) {
            isPromoting = false;
            refresh();
        }
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
        return new int[]{x, y};
    }

    private void emitPlayPos(double startXPos, double startYPos, double endXPos, double endYPos) {
        var startCell = getCell(startXPos, startYPos);
        var endCell = getCell(endXPos, endYPos);
        playCell(startCell[0], startCell[1], endCell[0], endCell[1], null);
    }

    private boolean playCell(int startX, int startY, int endX, int endY, ChessPiece promotionPiece) {
        if (ChessBoard.isOutside(endX, endY)) {
            return false;
        }

        boolean isPreMove = chessBoard.getChessPieceTeam(startX, startY) != currentTurn;
        if (handleMoveEvent(startX, startY, endX, endY, promotionPiece)) {
            if (!isPromoting && !isPreMove) {
                current_moveUCI = Character.toString(startX - 1 + 'a') + (9 - startY) + (char) (endX - 1 + 'a') + (9 - endY);
                gameSocket.sendMoveData(current_moveUCI);
            }
            if (isPreMove && promotionPiece != null) {
                chessBoard.getLastPreMove(0).promotedPiece = promotionPiece;
            }

            return true;
        }

        return false;
    }

    private void emitReceiveEvent(String moveUCI, boolean canDraw, String result) {
        drawButton.setDisable(!canDraw);
        rollbackAllPreMoves();

        if (moveUCI != null) {
            int startX = moveUCI.charAt(0) - 'a' + 1;
            int startY = 9 - Integer.parseInt(String.valueOf(moveUCI.charAt(1)));
            int endX = moveUCI.charAt(2) - 'a' + 1;
            int endY = 9 - Integer.parseInt(String.valueOf(moveUCI.charAt(3)));
            ChessPiece promotionPiece = null;
            if (isPromoting) {
                char promotionChar = moveUCI.charAt(4);
                ChessPiece.Team team = chessBoard.getChessPieceTeam(endX, endY);
                switch (promotionChar) {
                    case 'q':
                        promotionPiece = new Queen(team);
                        break;
                    case 'r':
                        promotionPiece = new Rook(team);
                        break;
                    case 'n':
                        promotionPiece = new Knight(team);
                        break;
                    case 'b':
                        promotionPiece = new Bishop(team);
                        break;
                }
            }

            if (promotionPiece != null) {
                addDragEvent(promotionPiece.getChessImage());
            }
            handleMoveEvent(startX, startY, endX, endY, promotionPiece);
        }

        if (result.equals("1/2-1/2")) {
            onGameOver("Draw");
        }
        else if (result.equals( "1-0")) {
            onGameOver("White-Win");
        } else if (result.equals( "0-1")) {
            onGameOver("Black-Win");
        } else {
            continuePreMove();
        }
    }

    private Pane getPaneFromGridPane(int column, int row) {
        for (Node node : chessBoardBox.getChildren()) {
            int columnIndex = GridPane.getColumnIndex(node);
            int rowIndex = GridPane.getRowIndex(node);

            if (columnIndex == column && rowIndex == row) {
                return (Pane) node;
            }
        }
        return null; // Trả về null nếu không tìm thấy phần tử tại vị trí (column, row)
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

    private boolean checkValidTurn(double posX, double posY) {
        ChessPiece.Team chessTeam = getTeam(posX, posY);
        if (playWithBot) {
            return chessTeam == playerTurn; // có thể pre move
        }
        return chessTeam == currentTurn;
    }

    public ChessPiece.Team getTeam(double posX, double posY) {
        var cell = getCell(posX, posY);
        return chessBoard.getChessPieceTeam(cell[0], cell[1]) ;
    }

    private void moveChessPane(int startX, int startY, int endX, int endY) {
        Pane startPane = getPaneFromGridPane(startX, startY);
        if (startX == 0 && startY == 0) {
            startPane.getChildren().add(saveChessImage);
        }
        if (endX == 0 && endY == 0) {
            saveChessImage = (ImageView) startPane.getChildren().removeLast();
            return;
        }

        Pane endPane = getPaneFromGridPane(endX, endY);

        Node chessImage = startPane.getChildren().removeLast();
        endPane.getChildren().setAll(chessImage);
    }

    public void continuePreMove() {
        List<PreMove> moves = chessBoard.continueInPreMoveList();
        for (PreMove move : moves) {
            if ((!chessBoard.existChessPiece(move.startX, move.startY) || chessBoard.getChessPieceTeam(move.startX, move.startY) != playerTurn)
                || (!playCell(move.startX, move.startY, move.endX, move.endY, move.promotedPiece))) {

                if (currentChooseImage != null) {
                    currentChooseImage.fireEvent(Utils.SecondaryMouse); // Thả
                }
                rollbackAllPreMoves();
                break;
            }
        }
    }

    private void onGameOver(String result) {
        gameOver = true;
        this.result = result;
        rollbackAndClearAllPreMoves();
        rollbackButton.setDisable(true);
        if (currentChooseImage != null) {
            currentChooseImage.fireEvent(Utils.SecondaryMouse); // Thả
        }

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> resetGame(true, false));
        }).start();
    }

    private void createPromotionBoard(int endX, int endY, boolean isPreMove) {
        ChessPiece.Team team = chessBoard.getChessPieceTeam(endX, endY); // Lấy team
        Pane[] chessPanes = {getPaneFromGridPane(endX, endY - 1), getPaneFromGridPane(endX + 1, endY),
                getPaneFromGridPane(endX, endY + 1), getPaneFromGridPane(endX - 1, endY)};

        ChessPiece[] promotionPieces = new ChessPiece[]{new Queen(team), new Rook(team), new Knight(team), new Bishop(team)};
        char[] promotionChars = new char[]{'q', 'r', 'n', 'b'};

        // Thêm 4 quân phong
        for (int i = 0; i < 4; i++) {
            // Phần pane trắng
            Pane promotionPane = new Pane();
            promotionPane.setStyle("-fx-background-color: white;");
            promotionPane.prefWidthProperty().bind(chessPanes[i].widthProperty());
            promotionPane.prefHeightProperty().bind(chessPanes[i].heightProperty());

            ChessPiece promotionPiece = promotionPieces[i];
            char promotionChar = promotionChars[i];
            ImageView promotionChessImage = promotionPiece.getChessImage();
            Utils.setImageToCell(promotionPane, promotionChessImage);
            chessPanes[i].getChildren().add(promotionPane);

            // Thêm sự kiện phong
            promotionChessImage.setOnMousePressed(event -> {
                // Kiểm tra chuột trái
                if (event.getButton() != MouseButton.PRIMARY) {
                    return;
                }

                setPromotionPiece(endX, endY, promotionPiece);
                addDragEvent(promotionChessImage);

                if (isPreMove) {
                    chessBoard.getLastPreMove(0).promotedPiece = promotionPiece;
                } else {
                    // Gửi move
                    current_moveUCI += promotionChar;
                    gameSocket.sendMoveData(current_moveUCI);
                }

                // Bỏ phần chọn phong
                for (Pane chessPane : chessPanes) {
                    chessPane.getChildren().removeLast();
                }
            });
        }
    }

    private void setPromotionPiece(int x, int y, ChessPiece promotionPiece) {
        ImageView promotionChessImage = promotionPiece.getChessImage();

        moveChessPane(x, y, 0, 0); // Save
        if (promotionChessImage == currentChooseImage) {
            Utils.setImageToCell(getPaneFromGridPane(x, y), currentCopyChooseImage);
        } else {
            Utils.setImageToCell(getPaneFromGridPane(x, y), promotionChessImage);
        }
        chessBoard.setPromote(x, y, promotionPiece);

        isPromoting = false;
    }

    private void highlightLastMoveEvent(boolean unhighlight) {
        Move lastMove = chessBoard.getLastMove(0);
        if (lastMove == null) {
            return;
        }
        if (lastMove.isSpecialMove) {
            lastMove = chessBoard.getLastMove(1); // đổi sang move event vì đang ở extra move
        }

        if (unhighlight) {
            ColorHighlighter.restoreOriginalColor(getPaneFromGridPane(lastMove.startX, lastMove.startY));
            ColorHighlighter.restoreOriginalColor(getPaneFromGridPane(lastMove.endX, lastMove.endY));
        } else {
            ColorHighlighter.highlightYellow(getPaneFromGridPane(lastMove.startX, lastMove.startY));
            ColorHighlighter.highlightYellow(getPaneFromGridPane(lastMove.endX, lastMove.endY));
        }
    }

    /**
     *
     * @return if move is valid
     */
    private boolean handleMoveEvent(int startX, int startY, int endX, int endY, ChessPiece promotionPiece) {
        // Kiểm tra nếu 2 ô trùng nhau
        if (startX == endX && startY == endY) {
            return false;
        }

        boolean isPreMove = chessBoard.getChessPieceTeam(startX, startY) != currentTurn;

        // Check valid move
        if ((!isPreMove && chessBoard.checkCanMovePiece(startX, startY, endX, endY)) ||
                (isPreMove && chessBoard.checkCanPreMovePiece(startX, startY, endX, endY))) {
            if (!isPreMove) {
                // Highlight yellow
                highlightLastMoveEvent(true);
            }

            chessBoard.moveChessPiece(startX, startY, endX, endY, isPreMove);
            moveChessPane(startX, startY, endX, endY);
            if (!isPreMove) {
                changeTurn();
            }

            // Đặc biệt thì di chuyển thêm lần nữa
            if (chessBoard.getLastMove(0).isSpecialMove) {
                Move extraMove = chessBoard.getLastMove(0);
                moveChessPane(extraMove.startX, extraMove.startY, extraMove.endX, extraMove.endY);
            }

            if (chessBoard.checkPromotion(endX, endY)) {
                if (promotionPiece == null) {
                    isPromoting = true;
                    createPromotionBoard(endX, endY, isPreMove);
                } else {
                    setPromotionPiece(endX, endY, promotionPiece);
                }
            }

            if (!isPreMove) {
                highlightLastMoveEvent(false);

                // play with bot => size > 1
                // play with player => size > 0
                if (chessBoard.getLastMove(playWithBot ? 1 : 0) == null) {
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

    private void addDragEvent(ImageView image) {
        // Tạo event kéo thả
        // Sự kiện nhấn chuột để lưu vị trí bắt đầu
        AtomicReference<Double> startXPos = new AtomicReference<>((double) 0);
        AtomicReference<Double> startYPos = new AtomicReference<>((double) 0);
        AtomicReference<Pane> containPane = new AtomicReference<>();
        AtomicReference<int[]> currentCell = new AtomicReference<>();
        AtomicReference<Boolean> isPreMove = new AtomicReference<>();
        AtomicInteger mouseStage = new AtomicInteger(); // 0: chưa chọn, 1: đang kéo
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
                // Lưu điểm bắt đầu
                startXPos.set(localInOverlayPane.getX());
                startYPos.set(localInOverlayPane.getY());

                currentCell.set(getCell(localInOverlayPane.getX(), localInOverlayPane.getY()));
                if (!chessBoard.existChessPiece(currentCell.get()[0], currentCell.get()[1]) || image != chessBoard.getChessPiece(currentCell.get()[0], currentCell.get()[1]).getChessImage()) { // Không trùng ảnh đang chọn
                    return;
                }

                // Không đúng lượt thì bỏ qua
                if (!checkValidTurn(startXPos.get(), startYPos.get())) {
                    return;
                }

                containPane.set(getPaneFromGridPane(image));
                ImageView copyImage = Utils.copyImageView(containPane.get(), image);
                copyImage.setOpacity(0.3); // Làm mờ
                containPane.get().getChildren().setAll(copyImage); // Đổi sang copy image

                currentChooseImage = image;
                currentCopyChooseImage = copyImage;

                overlayPane.getChildren().add(image); // Thêm vào overlayPane
                overlayPane.toFront(); // Đẩy lên trên cùng

                isPreMove.set(getTeam(startXPos.get(), startYPos.get()) != currentTurn);

                if (!isPreMove.get()) { // not pre move
                    ColorHighlighter.highlightYellow(containPane.get()); // tô màu vàng
                }

                if (!ChessBoard.isOutside(currentCell.get()[0], currentCell.get()[1])) {
                    getPaneFromGridPane(currentCell.get()[0], currentCell.get()[1]).getStyleClass().add("highlight-border"); // Thêm viền
                }

                image.setTranslateX(localInOverlayPane.getX() - image.getFitWidth() / 2);
                image.setTranslateY(localInOverlayPane.getY() - image.getFitHeight() / 2);

                mouseStage.set(1);
            }

            // Sự kiện huỷ
            if (event.getButton() == MouseButton.SECONDARY) {
                if (mouseStage.get() != 1) { // Không đúng stage
                    return;
                }

                image.setTranslateX(0);
                image.setTranslateY(0);

                if (!isPreMove.get()) { // not pre move
                    ColorHighlighter.restoreOriginalColor(containPane.get()); // bỏ màu vàng
                }

                overlayPane.getChildren().removeLast(); // Xóa khỏi overlayPane

                if (containPane.get().getChildren().isEmpty() || containPane.get().getChildren().getFirst() != currentCopyChooseImage) {
                    Pane findContainPane = getPaneFromGridPane(currentCopyChooseImage);
                    if (findContainPane != null) {
                        findContainPane.getChildren().setAll(image);
                    }
                } else {
                    containPane.get().getChildren().setAll(image); // Đổi lại image
                }

                currentChooseImage = null;
                currentCopyChooseImage = null;

                if (!ChessBoard.isOutside(currentCell.get()[0], currentCell.get()[1])) {
                    getPaneFromGridPane(currentCell.get()[0], currentCell.get()[1]).getStyleClass().remove("highlight-border"); // Xoá viền
                }
                chessBoardBox.toFront(); //Đẩy lại lên trên cùng

                mouseStage.set(0);
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
            if (currentCell.get() != curCell) {
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
            image.fireEvent(Utils.SecondaryMouse); // Huỷ

            Point2D localInOverlayPane = overlayPane.sceneToLocal(event.getSceneX(), event.getSceneY());
            double endXPos = localInOverlayPane.getX();
            double endYPos = localInOverlayPane.getY();

            // Kích hoạt sự kiện
            emitPlayPos(startXPos.get(), startYPos.get(), endXPos, endYPos);
        });
    }

    @Override
    public void refresh() {
        addNumOrder();
        for (Node node : chessBoardBox.getChildren()) {
            int x = GridPane.getColumnIndex(node);
            int y = GridPane.getRowIndex(node);
            if (x < 1 || x > 8 || y < 1 || y > 8) {
                continue;
            }

            Pane cell = (Pane) node;
            if (chessBoard.existChessPiece(x, y)) {
                ImageView chessImage = chessBoard.getChessPiece(x, y).getChessImage();

                Utils.setImageToCell(cell, chessImage);

                addDragEvent(chessImage);
            } else {
                cell.getChildren().clear();
            }
        }
    }

    private void initChessBoard() {
        chessBoard = new ChessBoard();

        mainBox.setPadding(new Insets(2, 0, 2, 0)); // Giảm viền trên và dưới

        chessBoardBox.getColumnConstraints().clear();
        chessBoardBox.getRowConstraints().clear();
        chessBoardBox.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                rollbackAllPreMoves();
                chessBoard.continueInPreMoveList();
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
                        () -> Math.min(mainBox.getHeight(), mainBox.getWidth()),
                        mainBox.heightProperty(),
                        mainBox.widthProperty()
                )
        );
        chessBoardBox.prefHeightProperty().bind(chessBoardBox.prefWidthProperty());

        // Color chess board
        for (int row = 0; row <= 9; row++) {
            for (int col = 0; col <= 9; col++) {
                Pane cell = new Pane(); // Create new cell

                if (1 <= row && row <= 8 && 1 <= col && col <= 8) {
                    // if (row + col) even, color red
                    if ((row + col) % 2 == 0) {
                        cell.setStyle("-fx-background-color: #eeeed2;");
                    } else {
                        cell.setStyle("-fx-background-color: #769656;");
                    }
                }

                chessBoardBox.add(cell, col, row); // Add to GridPane
            }
        }
    }

    private void initGameplay(boolean playWithBot, boolean humanPlayFirst) {
        this.playWithBot = playWithBot;
        this.playerTurn = humanPlayFirst ? ChessPiece.Team.WHITE : ChessPiece.Team.BLACK;
        this.currentTurn = ChessPiece.Team.WHITE;
        this.isPromoting = false;
        this.gameOver = false;
        drawButton.setDisable(true);
        rollbackButton.setDisable(true);

        highlightLastMoveEvent(true);
        chessBoard.initChessBoard();

        gameSocket.sendResetData(humanPlayFirst);
    }

    private void addNumOrder() {
        final String numLabelStyle = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #9e7357;";

        for (int row = 1; row <= 8; row++) {
            Label numLabel1 = new Label(String.valueOf((char) ('a' + row - 1)));
            Label numLabel2 = new Label(String.valueOf((char) ('a' + row - 1)));
            numLabel1.setStyle(numLabelStyle);
            numLabel2.setStyle(numLabelStyle);
            VBox vbox1 = new VBox(numLabel1);
            VBox vbox2 = new VBox(numLabel2);
            vbox1.setAlignment(Pos.BOTTOM_CENTER);
            vbox2.setAlignment(Pos.TOP_CENTER);

            Pane pane1 = getPaneFromGridPane(row, 0);
            Pane pane2 = getPaneFromGridPane(row, 9);

            vbox1.prefHeightProperty().bind(pane1.heightProperty());
            vbox1.prefWidthProperty().bind(pane1.widthProperty());

            vbox2.prefHeightProperty().bind(pane2.heightProperty());
            vbox2.prefWidthProperty().bind(pane2.widthProperty());

            pane1.getChildren().setAll(vbox1);
            pane2.getChildren().setAll(vbox2);
        }

        for (int col = 1; col <= 8; col++) {
            Label numLabel1 = new Label((9 - col) + " ");
            Label numLabel2 = new Label(" " + (9 - col));
            numLabel1.setStyle(numLabelStyle);
            numLabel2.setStyle(numLabelStyle);
            HBox vbox1 = new HBox(numLabel1);
            HBox vbox2 = new HBox(numLabel2);
            vbox1.setAlignment(Pos.CENTER_RIGHT);
            vbox2.setAlignment(Pos.CENTER_LEFT);

            Pane pane1 = getPaneFromGridPane(0, col);
            Pane pane2 = getPaneFromGridPane(9, col);

            vbox1.prefHeightProperty().bind(pane1.heightProperty());
            vbox1.prefWidthProperty().bind(pane1.widthProperty());

            vbox2.prefHeightProperty().bind(pane2.heightProperty());
            vbox2.prefWidthProperty().bind(pane2.widthProperty());

            pane1.getChildren().setAll(vbox1);
            pane2.getChildren().setAll(vbox2);
        }
    }

    private void resetGame(boolean playWithBot, boolean humanPlayFirst) {
        initGameplay(playWithBot, humanPlayFirst);
        refresh();
    }

    public void initSocket() {
        gameSocket = new GameSocket();
        gameSocket.setListener(new SocketListener() {
            @Override
            public void onMoveReceived(String moveUCI, boolean canDraw, String result) {
                Platform.runLater(() -> emitReceiveEvent(moveUCI, canDraw, result));
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

    @FXML
    private void initialize() {
        initSocket();
        initChessBoard();
        resetGame(true, false);
    }
}
