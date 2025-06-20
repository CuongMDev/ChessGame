package Utils;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class ColorHighlighter {
    private static final double BLEND_FACTOR = 0.3;

    // Lấy màu nền hiện tại từ style CSS "-fx-background-color"
    public static Color getBackgroundColorFromStyle(Pane pane) {
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

    // Blend màu vàng lên màu nền pane theo blendFactor
    public static void highlightYellow(Pane pane) {
        Color base = getBackgroundColorFromStyle(pane);

        double r = base.getRed()   * (1 - BLEND_FACTOR) + 1.0 * BLEND_FACTOR;
        double g = base.getGreen() * (1 - BLEND_FACTOR) + 1.0 * BLEND_FACTOR;
        double b = base.getBlue()  * (1 - BLEND_FACTOR) + 0.0 * BLEND_FACTOR;

        Color blended = new Color(r, g, b, base.getOpacity());

        setBackgroundColor(pane, blended);
    }

    // Khôi phục màu gốc dựa trên màu đã blend và blendFactor
    public static void restoreOriginalColor(Pane pane) {
        Color blended = getBackgroundColorFromStyle(pane);
        Color original = calculateOriginalColor(blended, BLEND_FACTOR);

        setBackgroundColor(pane, original);
    }

    // Tính màu gốc từ màu blend và blendFactor
    private static Color calculateOriginalColor(Color blended, double blendFactor) {
        if (blendFactor >= 1.0) throw new IllegalArgumentException("blendFactor must be < 1");

        double r = (blended.getRed()   - blendFactor) / (1 - blendFactor);
        double g = (blended.getGreen() - blendFactor) / (1 - blendFactor);
        double b = blended.getBlue() / (1 - blendFactor);

        r = clamp(r);
        g = clamp(g);
        b = clamp(b);

        return new Color(r, g, b, blended.getOpacity());
    }

    // Giới hạn giá trị màu trong khoảng 0..1
    private static double clamp(double val) {
        if (val < 0) return 0;
        if (val > 1) return 1;
        return val;
    }

    // Đặt màu nền cho Pane dưới dạng CSS rgba()
    private static void setBackgroundColor(Pane pane, Color color) {
        String rgba = String.format("rgba(%d,%d,%d,%.2f)",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255),
                color.getOpacity());
        pane.setStyle("-fx-background-color: " + rgba + ";");
    }
}