package org.example.chessgame.Utils;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.Deque;

public class ColorHighlighter {
    private static final double BLEND_FACTOR = 0.5;

    Deque<Color>[][] colorBoard;

    public ColorHighlighter() {
        colorBoard = new ArrayDeque[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                colorBoard[i][j] = new ArrayDeque<>();
            }
        }
    }

    // Lấy màu nền hiện tại từ style CSS "-fx-background-color"
    public Color getBackgroundColorFromStyle(Pane pane) {
        String style = pane.getStyle();
        if (style == null) return Color.WHITE;

        for (String entry : style.split(";")) {
            if (entry.trim().startsWith("-fx-background-color")) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    String colorStr = parts[1].trim();
                    try {
                        return Color.web(colorStr);
                    } catch (Exception e) {
                        return Color.WHITE;
                    }
                }
            }
        }
        return Color.WHITE;
    }

    public Color getLastColor(int x, int y) {
        if (colorBoard[x][y].isEmpty()) return null;
        return colorBoard[x][y].getLast();
    }

    public void addColorLast(Pane pane, int x, int y, Color color) {
        if (!colorBoard[x][y].isEmpty()) {
            restoreColor(pane, x, y);
        }

        colorBoard[x][y].addLast(color);
        highlightColor(pane, x, y, color);
    }

    public void addColorFirst(Pane pane, int x, int y, Color color) {
        colorBoard[x][y].addFirst(color);
        if (colorBoard[x][y].size() == 1) {
            highlightColor(pane, x, y, color);
        }
    }

    // Khôi phục màu gốc dựa trên màu đã blend và blendFactor
    public void popColorLast(Pane pane, int x, int y) {
        restoreColor(pane, x, y);
        colorBoard[x][y].removeLast();
        if (!colorBoard[x][y].isEmpty()) {
            highlightColor(pane, x, y, colorBoard[x][y].getLast());
        }
    }

    public void popColorFirst(Pane pane, int x, int y) {
        if (colorBoard[x][y].size() == 1) {
            restoreColor(pane, x, y);
        }
        colorBoard[x][y].removeFirst();
    }

    // Blend màu vàng/đỏ lên màu nền pane theo blendFactor
    private void highlightColor(Pane pane, int x, int y, Color color) {
        Color base = getBackgroundColorFromStyle(pane);

        double r = base.getRed()   * (1 - BLEND_FACTOR) + 1.0 * BLEND_FACTOR;
        double g = base.getGreen() * (1 - BLEND_FACTOR) + (color == Color.YELLOW ? 1 : 0) * BLEND_FACTOR;
        double b = base.getBlue()  * (1 - BLEND_FACTOR) + 0.0 * BLEND_FACTOR;

        Color blended = new Color(r, g, b, base.getOpacity());

        setBackgroundColor(pane, blended);
    }

    private void restoreColor(Pane pane, int x, int y) {
        Color blended = getBackgroundColorFromStyle(pane);
        Color original = calculateOriginalColor(x, y, blended, BLEND_FACTOR);

        setBackgroundColor(pane, original);
    }

    // Tính màu gốc từ màu blend và blendFactor
    private Color calculateOriginalColor(int x, int y, Color blended, double blendFactor) {
        if (blendFactor >= 1.0) throw new IllegalArgumentException("blendFactor must be < 1");

        double r = (blended.getRed()   - blendFactor) / (1 - blendFactor);
        double g = (blended.getGreen() - (colorBoard[x][y].getLast() == Color.YELLOW ? blendFactor : 0)) / (1 - blendFactor);
        double b = blended.getBlue() / (1 - blendFactor);

        r = clamp(r);
        g = clamp(g);
        b = clamp(b);

        return new Color(r, g, b, blended.getOpacity());
    }

    // Giới hạn giá trị màu trong khoảng 0..1
    private double clamp(double val) {
        if (val < 0) return 0;
        if (val > 1) return 1;
        return val;
    }

    // Đặt màu nền cho Pane dưới dạng CSS rgba()
    private void setBackgroundColor(Pane pane, Color color) {
        String rgba = String.format("rgba(%d,%d,%d,%.2f)",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255),
                color.getOpacity());
        pane.setStyle("-fx-background-color: " + rgba + ";");
    }
}