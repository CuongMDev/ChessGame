module org.example.chessgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.chessgame to javafx.fxml;
    exports org.example.chessgame;
}