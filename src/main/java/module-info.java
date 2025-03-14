module org.example.chessgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.chessgame to javafx.fxml;
    exports org.example.chessgame;
    exports org.example.chessgame.GameBoard;
    opens org.example.chessgame.GameBoard to javafx.fxml;
}