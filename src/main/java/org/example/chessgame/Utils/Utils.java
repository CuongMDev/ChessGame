package org.example.chessgame.Utils;

import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

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

    public static ImageView copyImageView(Pane container, ImageView original) {
        Image originalImage = original.getImage();
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();

        WritableImage newImage = new WritableImage(width, height);
        PixelReader reader = originalImage.getPixelReader();
        PixelWriter writer = newImage.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setArgb(x, y, reader.getArgb(x, y));
            }
        }

        ImageView copyImage = new ImageView(newImage);
        copyImage.setFitWidth(container.getWidth());
        copyImage.setFitHeight(container.getHeight());
        return copyImage;
    }

    public static void setImageToCell(Pane cell, ImageView image) {
        image.fitWidthProperty().bind(cell.widthProperty());
        image.fitHeightProperty().bind(cell.heightProperty());

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
