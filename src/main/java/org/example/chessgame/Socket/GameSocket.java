package org.example.chessgame.Socket;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class GameSocket {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private SocketListener listener;
    private Process aiPythonProcess;

    public void runAiPython() throws IOException {
        // Lệnh chạy file Python
        ProcessBuilder processBuilder = new ProcessBuilder("python", "-m", "Play.game_socket");
        processBuilder.directory(new File("C:\\Users\\mcuon\\PycharmProjects\\ChessProject\\"));
        // Chạy tiến trình
        aiPythonProcess = processBuilder.start();

        // Đọc output từ Python để kiểm tra server đã chạy chưa
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(aiPythonProcess.getInputStream()));
        String line;
        while ((line = stdInput.readLine()) != null) {
            System.out.println("[Python]: " + line);
            if (line.contains("Server Python Ready")) {  // Kiểm tra thông báo server đã sẵn sàng
                break;
            }
        }
    }

    public void read() {
        // Đọc stderr
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(aiPythonProcess.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("Python stderr: " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(aiPythonProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("Python stderr: " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void quitAiPython() {
        if (aiPythonProcess != null && aiPythonProcess.isAlive()) {
            aiPythonProcess.destroy(); // Yêu cầu Python thoát
            System.out.println("Đã gửi lệnh tắt Python.");
        }
    }

    public void setListener(SocketListener listener) {
        this.listener = listener;
    }

    public void startServer() {
        connectToServer();
    }

    private void connectToServer() {
        String serverAddress = "127.0.0.1";
        int port = 12345;
        try {
            runAiPython();

            System.out.println("Đang chờ client");
            socket = new Socket(serverAddress, port);
            System.out.println("Socket đã kết nối!");
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Kết nối tới server thành công!");

            new Thread(this::receiveData).start(); // Luồng riêng nhận dữ liệu

        } catch (IOException e) {
            if (listener != null) {
                e.printStackTrace();
                quitAiPython();
                listener.onError("Không thể kết nối tới server!");
            }
        }
    }

    public void sendMoveData(String moveUCI) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("move_uci", moveUCI);
        out.println(jsonData);
    }

    public void sendResetData(boolean humanPlayFirst, String fen) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("reset", 1);
        jsonData.put("human_play_first", humanPlayFirst);
        jsonData.put("fen", fen);
        out.println(jsonData);
    }

    public void sendRollbackData() {
        JSONObject jsonData = new JSONObject();
        jsonData.put("rollback", 1);
        out.println(jsonData);
    }

    private void receiveData() {
        try {
            while (true) {
                if (!in.ready()) { // 🔹 Kiểm tra xem có dữ liệu không
                    continue;
                }
                String response = in.readLine();
                if (response == null) break;

                JSONObject jsonResponse = new JSONObject(response);

                if (jsonResponse.has("move_uci") && jsonResponse.has("can_draw") && jsonResponse.has("result")) {
                    String moveUCI = jsonResponse.isNull("move_uci") ? null : jsonResponse.getString("move_uci");
                    boolean canDraw = jsonResponse.getBoolean("can_draw");
                    String result = jsonResponse.getString("result");

                    if (listener != null) {
                        listener.onMoveReceived(moveUCI, canDraw, result); // Gọi event
                    }
                } else {
                    if (listener != null) {
                        listener.onError("Lỗi từ server: " + jsonResponse.getString("error"));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            listener.onError(e.getMessage());
        }
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                in.close();
                out.close();
                socket.close();
            }
            quitAiPython();
        }
        catch (Exception e) {

        }
    }
}
