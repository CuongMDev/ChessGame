package org.example.chessgame.GameBoard;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.chessgame.Abstract.Controller;
import org.example.chessgame.ChessObject.ChessBoard;
import org.example.chessgame.ChessObject.ChessPiece;
import org.example.chessgame.ChessObject.Move;

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
        if (endX == 0 && endY == 0) {
            startPane.getChildren().removeLast();
            return;
        }

        Pane endPane = getPaneFromGridPane(endX, endY);

        Node chessImage = startPane.getChildren().removeLast();
        endPane.getChildren().setAll(chessImage);
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

                if (chessBoard.getLastMove(0).isSpecialMove) {
                    Move getLast1Move = chessBoard.getLastMove(1);
                    moveChessPane(getLast1Move.startX, getLast1Move.startY, getLast1Move.endX, getLast1Move.endY);
                }

                changeTurn();
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
            if (!checkValidTurn(startXPos.get(), startYPos.get())) {
                return;
            }

            Point2D d = overlayPane.sceneToLocal(event.getSceneX(), event.getSceneY());
            image.setTranslateX(d.getX() - image.getFitWidth() / 2);
            image.setTranslateY(d.getY() - image.getFitHeight() / 2);
        });

        // Sự kiện thả
        image.setOnMouseReleased(event -> {
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

    @Override
    public void refresh() {
        for (Node node : chessBoardBox.getChildren()) {
            int x = GridPane.getColumnIndex(node);
            int y = GridPane.getRowIndex(node);
            if (x < 1 || x > 8 || y < 1 || y > 8) {
                continue;
            }

            Pane cell = (Pane) node;
            if (chessBoard.existChessPiece(x, y)) {
                ImageView chessImage = chessBoard.getChessPiece(x, y).getChessImage();
                chessImage.fitWidthProperty().bind(cell.widthProperty());
                chessImage.fitHeightProperty().bind(cell.heightProperty());

                addDragEvent(chessImage);

                cell.getChildren().setAll(chessImage);
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
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                Pane cell = new Pane(); // Create new cell

                // if (row + col) even, color red
                if ((row + col) % 2 == 0) {
                    cell.setStyle("-fx-background-color: #a3683c;");
                } else {
                    cell.setStyle("-fx-background-color: #e9c7ac;");
                }

                chessBoardBox.add(cell, col, row); // Add to GridPane
            }
        }

        addNumOrder();
    }

    private void initGameplay() {
        playerTurn = ChessPiece.Team.WHITE;
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

            chessBoardBox.add(vbox1, row, 0);
            chessBoardBox.add(vbox2, row, 9);
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

            chessBoardBox.add(vbox1, 0, col);
            chessBoardBox.add(vbox2, 9, col);
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
