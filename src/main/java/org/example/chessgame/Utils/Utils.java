package org.example.chessgame.Utils;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class Utils {

    public static MouseEvent SecondaryMouse = new MouseEvent(
            MouseEvent.MOUSE_PRESSED, // Loại sự kiện
            0, 0, // x, y trong node
            0, 0, // screenX, screenY
            MouseButton.SECONDARY, // Chuột phải
            1, // click count
            false, false, false, false, // shift, ctrl, alt, meta
            false, false, true, // primary, middle, secondary down
            true, // synthesized
            false, false, null // pickResult
    );

    public static Point2D sceneToScene(double x, double y, Pane paneA, Pane paneB) {
        Point2D pointInScene = paneA.localToScene(x, y);
        return paneB.sceneToLocal(pointInScene);
    }

    public static void setImageToCell(Pane cell, StackPane image) {
        image.prefWidthProperty().bind(cell.widthProperty());
        image.prefHeightProperty().bind(cell.heightProperty());

        cell.getChildren().setAll(image);
    }

    // Thêm result vào PGN nếu chưa có
    public static String addResultToPGN(String pgn, String result) {
        return pgn.replace("[Result \"*\"]", "[Result \"" + result + "\"]");
    }

    public static void copyToClipBoard(String str) {
        // Copy vào clipboard
        StringSelection selection = new StringSelection(str);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }
}
