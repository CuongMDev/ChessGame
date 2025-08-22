package org.example.chessgame.Socket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameSocket {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private SocketListener listener;
    private Process aiPythonProcess;

    private final Gson gson = new Gson();

    private volatile boolean running = true;

    public void runAiPython() throws IOException {
        ProcessBuilder processBuilder;

        // Lệnh chạy file Python
        String mode = System.getProperty("mode", "release");
        if ("debug".equalsIgnoreCase(mode)) {
            processBuilder = new ProcessBuilder("python", "-m", "Play.game_socket");
            processBuilder.directory(new File("C:\\Users\\mcuon\\PycharmProjects\\ChessProject\\"));
        }
        else {
            Path currentDir = Paths.get(System.getProperty("user.dir"));
            Path exePath = currentDir.resolve("runtime").resolve("GameProcess").resolve("game_socket").resolve("game_socket.exe");
            if (!exePath.toFile().exists()) {
                exePath = currentDir.resolve("GameProcess").resolve("game_socket").resolve("game_socket.exe");
            }
            processBuilder = new ProcessBuilder(exePath.toString());
            processBuilder.directory(exePath.getParent().toFile());
        }
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
        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("move_uci", moveUCI);
        out.println(gson.toJson(jsonData));
    }

    public void sendResetData(boolean playWithBot, boolean humanPlayFirst, String fen) {
        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("reset", 1);
        jsonData.addProperty("play_with_bot", playWithBot);
        jsonData.addProperty("human_play_first", humanPlayFirst);
        jsonData.addProperty("fen", fen);
        out.println(gson.toJson(jsonData));
    }

    public void sendChangeThinkingAbilityData(double thinkingAbility, double searchThread) {
        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("thinking_ability", thinkingAbility);
        jsonData.addProperty("search_thread", searchThread);
        out.println(gson.toJson(jsonData));
    }

    public void sendRollbackData() {
        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("rollback", 1);
        out.println(gson.toJson(jsonData));
    }

    public void sendRequirePGNData() {
        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("require_pgn", 1);
        out.println(gson.toJson(jsonData));
    }

    private void receiveData() {
        try {
            while (running) {
                if (!in.ready()) {
                    continue;
                }

                String response = in.readLine();
                if (response == null) break;

                JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

                if (jsonResponse.has("move_uci") && jsonResponse.has("can_draw") && jsonResponse.has("result")) {
                    String moveUCI = jsonResponse.get("move_uci").isJsonNull() ? null : jsonResponse.get("move_uci").getAsString();
                    boolean canDraw = jsonResponse.get("can_draw").getAsBoolean();
                    String result = jsonResponse.get("result").getAsString();

                    if (listener != null) {
                        listener.onMoveReceived(moveUCI, canDraw, result);
                    }
                } else if (jsonResponse.has("pgn")) {
                    String pgn = jsonResponse.get("pgn").getAsString();
                    if (listener != null) {
                        listener.onPGNReceived(pgn);
                    }
                } else if (jsonResponse.has("error")) {
                    if (listener != null) {
                        listener.onError("Lỗi từ server: " + jsonResponse.get("error").getAsString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            listener.onError(e.getMessage());
        }
    }

    public void close() {
        running = false;
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
