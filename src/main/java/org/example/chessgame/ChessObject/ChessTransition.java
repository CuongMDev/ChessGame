package org.example.chessgame.ChessObject;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;

public class ChessTransition extends AnimationTimer {
    private final Node node;
    private final double startX, startY;
    private final double targetX, targetY;
    private final double durationMillis; // thời gian di chuyển (ms)
    private long startTime = 0;
    private Runnable onFinished;

    public ChessTransition(Node node, double targetX, double targetY, double durationMillis) {
        this.node = node;
        this.targetX = targetX;
        this.targetY = targetY;
        this.durationMillis = durationMillis;

        this.startX = node.getTranslateX();
        this.startY = node.getTranslateY();
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    @Override
    public void stop() {
        super.stop();

        if (onFinished != null) {
            onFinished.run(); // gọi callback
            onFinished = null;
        }
    }

    @Override
    public void handle(long now) {
        if (startTime == 0) startTime = now;

        double elapsedMillis = (now - startTime) / 1_000_000.0; // ns -> ms
        double t = Math.min(elapsedMillis / durationMillis, 1.0); // 0 → 1

        double tEased = t * t; // ease-in (bắt đầu chậm)
        double newX = startX + (targetX - startX) * tEased;
        double newY = startY + (targetY - startY) * tEased;

        node.setTranslateX(newX);
        node.setTranslateY(newY);

        if (t >= 1.0) {
            stop(); // dừng animation
        }
    }
}
