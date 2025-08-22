package org.example.chessgame.Socket;

public interface SocketListener {
    void onMoveReceived(String moveUCI, boolean canDraw, String result);
    void onPGNReceived(String pgn);
    void onError(String message);
}
