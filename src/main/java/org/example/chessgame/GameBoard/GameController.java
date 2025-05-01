package org.example.chessgame.GameBoard;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.example.chessgame.Abstract.Controller;
import org.example.chessgame.ChessObject.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GameController extends Controller {
    @FXML
    GridPane chessBoardBox;
    @FXML
    HBox mainBox;
    @FXML
    Pane overlayPane;
    ChessPiece.Team playerTurn;

    ChessBoard chessBoard;

    ImageView saveChessImage;

    boolean isPromotion;

    @FXML
    private void onRollbackClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (isPromotion) {
                refresh();
                isPromotion = false;
            }

            List<Move> moveList = chessBoard.rollback();
            if (moveList == null) {
                return;
            }

            for (Move move : moveList) {
                moveChessPane(move.endX, move.endY, move.startX, move.startY);
                if (move.deadPiece != null) {
                    getPaneFromGridPane(move.endX, move.endY).getChildren().setAll(move.deadPiece.getChessImage());
                }
            }
            changeTurn();
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

    private void emitPlayCell(double startXPos, double startYPos, double endXPos, double endYPos) {
        var startCell = getCell(startXPos, startYPos);
        var endCell = getCell(endXPos, endYPos);

        if (chessBoard.isOutside(endCell[0], endCell[1])) {
            return;
        }

        handlePlayerEvent(startCell[0], startCell[1], endCell[0], endCell[1]);
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
        var cell = getCell(posX, posY);
        return chessBoard.getChessPieceTeam(cell[0], cell[1]) == playerTurn;
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

    private void createPromotionBoard(int endX, int endY) {
        isPromotion = true;
        ChessPiece.Team team = chessBoard.getChessPieceTeam(endX, endY); // Lấy team
        Pane[] chessPanes = {getPaneFromGridPane(endX, endY - 1), getPaneFromGridPane(endX + 1, endY),
                getPaneFromGridPane(endX, endY + 1), getPaneFromGridPane(endX - 1, endY)};
        ChessPiece[] promotionPieces = {new Queen(team), new Rook(team), new Knight(team), new Bishop(team)};

        // Thêm 4 quân phong
        for (int i = 0; i < 4; i++) {
            Pane promotionPane = new Pane();
            promotionPane.setStyle("-fx-background-color: white;");
            promotionPane.prefWidthProperty().bind(chessPanes[i].widthProperty());
            promotionPane.prefHeightProperty().bind(chessPanes[i].heightProperty());

            ChessPiece promotionPiece = promotionPieces[i];
            ImageView promotionChessImage = promotionPiece.getChessImage();
            setChessPiece(promotionPane, promotionChessImage);
            chessPanes[i].getChildren().add(promotionPane);

            // Thêm sự kiện phong
            promotionChessImage.setOnMousePressed(event -> {
                // Kiểm tra chuột trái
                if (event.getButton() != MouseButton.PRIMARY) {
                    return;
                }

                moveChessPane(endX, endY, 0, 0); // Save
                addDragEvent(promotionChessImage);
                getPaneFromGridPane(endX, endY).getChildren().setAll(promotionChessImage);
                chessBoard.setPromote(endX, endY, promotionPiece);

                // Bỏ phần chọn phong
                for (Pane chessPane : chessPanes) {
                    chessPane.getChildren().removeLast();
                }
                isPromotion = false;
            });
        }
    }

    private void handlePlayerEvent(int startX, int startY, int endX, int endY) {
        // Kiểm tra nếu 2 ô trùng nhau
        if (startX == endX && startY == endY) {
            return;
        }

        //check valid turn
        if (chessBoard.getChessPieceTeam(startX, startY) == playerTurn) {
            // Check valid move
            if (chessBoard.moveChessPiece(startX, startY, endX, endY)) {
                moveChessPane(startX, startY, endX, endY);
                changeTurn();

                if (chessBoard.checkPromotion(endX, endY)) {
                    createPromotionBoard(endX, endY);
                }

                // Đặc biệt thì di chuyển thêm lần nữa
                if (chessBoard.getLastMove(0).isSpecialMove) {
                    Move getLast1Move = chessBoard.getLastMove(1);
                    moveChessPane(getLast1Move.startX, getLast1Move.startY, getLast1Move.endX, getLast1Move.endY);
                }
            }
        }
    }

    void changeTurn() {
        if (playerTurn == ChessPiece.Team.BLACK) {
            playerTurn = ChessPiece.Team.WHITE;
        } else {
            playerTurn = ChessPiece.Team.BLACK;
        }
    }

    private void addDragEvent(ImageView image) {
        // Tạo event kéo thả
        // Sự kiện nhấn chuột để lưu vị trí bắt đầu
        AtomicReference<Double> startXPos = new AtomicReference<>((double) 0);
        AtomicReference<Double> startYPos = new AtomicReference<>((double) 0);
        AtomicReference<Pane> containPane = new AtomicReference<>();
        image.setOnMousePressed(event -> {
            if (event.getButton() != MouseButton.PRIMARY || isPromotion) {
                return;
            }
            Point2D localInOverlayPane = overlayPane.sceneToLocal(event.getSceneX(), event.getSceneY());
            // Lưu điểm bắt đầu
            startXPos.set(localInOverlayPane.getX());
            startYPos.set(localInOverlayPane.getY());

            // Không đúng lượt thì bỏ qua
            if (!checkValidTurn(startXPos.get(), startYPos.get())) {
                return;
            }

            // Nếu chưa ở overlayPane thì di chuyển sang
            if (image.getParent() != overlayPane) {
                containPane.set(getPaneFromGridPane(image));
                containPane.get().getChildren().removeLast(); // Xóa khỏi containPane
                overlayPane.getChildren().add(image); // Thêm vào overlayPane
                overlayPane.toFront(); //Đẩy lên trên cùng
            }

            image.setTranslateX(localInOverlayPane.getX() - image.getFitWidth() / 2);
            image.setTranslateY(localInOverlayPane.getY() - image.getFitHeight() / 2);
        });

        // Sự kiện kéo thả
        image.setOnMouseDragged(event -> {
            if (event.getButton() != MouseButton.PRIMARY || isPromotion) {
                return;
            }
            if (!checkValidTurn(startXPos.get(), startYPos.get())) {
                return;
            }

            Point2D d = overlayPane.sceneToLocal(event.getSceneX(), event.getSceneY());
            image.setTranslateX(d.getX() - image.getFitWidth() / 2);
            image.setTranslateY(d.getY() - image.getFitHeight() / 2);
        });

        // Sự kiện thả
        image.setOnMouseReleased(event -> {
            if (event.getButton() != MouseButton.PRIMARY || isPromotion) {
                return;
            }
            if (!checkValidTurn(startXPos.get(), startYPos.get())) {
                return;
            }

            image.setTranslateX(0);
            image.setTranslateY(0);
            overlayPane.getChildren().removeLast(); // Xóa khỏi overlayPane
            containPane.get().getChildren().add(image); // Thêm lại vào containPane
            chessBoardBox.toFront(); //Đẩy lại lên trên cùng

            Point2D localInOverlayPane = overlayPane.sceneToLocal(event.getSceneX(), event.getSceneY());
            double endXPos = localInOverlayPane.getX();
            double endYPos = localInOverlayPane.getY();

            // Kích hoạt sự kiện
            emitPlayCell(startXPos.get(), startYPos.get(), endXPos, endYPos);
        });
    }

    public void setChessPiece(Pane cell, ImageView chessPieceImage) {
        chessPieceImage.fitWidthProperty().bind(cell.widthProperty());
        chessPieceImage.fitHeightProperty().bind(cell.heightProperty());

        cell.getChildren().setAll(chessPieceImage);
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

                setChessPiece(cell, chessImage);

                addDragEvent(chessImage);
            } else {
                cell.getChildren().clear();
            }
        }
    }

    void initChessPiece() {
        chessBoard = new ChessBoard();
        chessBoard.initChessBoard();
    }

    private void initChessBoard() {
        mainBox.setPadding(new Insets(2, 0, 2, 0)); // Giảm viền trên và dưới
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
                        cell.setStyle("-fx-background-color: #a3683c;");
                    } else {
                        cell.setStyle("-fx-background-color: #e9c7ac;");
                    }
                }

                chessBoardBox.add(cell, col, row); // Add to GridPane
            }
        }
    }

    private void initGameplay() {
        playerTurn = ChessPiece.Team.WHITE;
        isPromotion = false;
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

    private void resetGame() {
        initChessPiece();
        initGameplay();
        refresh();
    }

    @FXML
    private void initialize() {
        initChessBoard();
        initChessPiece();
        initGameplay();
        refresh();
    }
}
