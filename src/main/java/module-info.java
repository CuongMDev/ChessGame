module org.example.chessgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.json;
    requires javafx.media;


    opens org.example.chessgame to javafx.fxml;
    exports org.example.chessgame;
    exports org.example.chessgame.GameBoard;
    opens org.example.chessgame.GameBoard to javafx.fxml;
    exports org.example.chessgame.Menu;
    opens org.example.chessgame.Menu to javafx.fxml;
    exports org.example.chessgame.Menu.Setting;
    opens org.example.chessgame.Menu.Setting to javafx.fxml;
    exports org.example.chessgame.Sound;
    opens org.example.chessgame.Sound to javafx.fxml;
}