package org.example.chessgame;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.chessgame.Abstract.Controller;
import org.example.chessgame.Menu.MenuController;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class Main extends Application {
    MenuController menuController;

    private static FileLock lock;
    private static FileChannel channel;
    private static RandomAccessFile randomAccessFile;

    @Override
    public void start(Stage stage) throws IOException {
        menuController = (MenuController) Controller.init(stage, getClass().getResource("Menu/Menu.fxml"));
        stage.setTitle("Chess!");
        stage.getIcons().add(new Image("file:icon.png"));
        stage.setScene(menuController.getParent().getScene());
        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest(event -> {
        });
    }

    @Override
    public void stop() throws Exception {
        menuController.closeSocket();
    }

    private static boolean lockInstance(String lockFile) {
        try {
            File file = new File(lockFile);
            randomAccessFile = new RandomAccessFile(file, "rw");
            channel = randomAccessFile.getChannel();

            lock = channel.tryLock();
            if (lock == null) {
                channel.close();
                randomAccessFile.close();
                return false;
            }

            // Khi app tắt → giải phóng lock
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (lock != null) lock.release();
                    if (channel != null) channel.close();
                    if (randomAccessFile != null) randomAccessFile.close();
                } catch (Exception ignored) {}
            }));

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        if (!lockInstance("app.lock")) {
            System.out.println("Ứng dụng đã chạy rồi, không thể mở thêm!");
            System.exit(0);
        }
        launch();
    }
}