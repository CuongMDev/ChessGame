package org.example.chessgame.Abstract;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public abstract class Controller implements Refreshable {
    private Stage stage;
    private FXMLLoader fxmlLoaders;
    private Parent parent;
    private Parent savePane;

    public static Controller init(Stage stage, URL location) throws IOException {
        FXMLLoader fxmlLoaders = new FXMLLoader(location);
        Parent parent = fxmlLoaders.load();
        new Scene(parent);

        Controller controller = fxmlLoaders.getController();
        controller.setStage(stage);
        controller.setParent(parent);

        return controller;
    }

    @Override
    public void refresh() {

    }

    public Controller getController() {
        return fxmlLoaders.getController();
    }

    public void switchToSavePane(StackPane stackPane) {
        stackPane.getChildren().setAll(savePane);
    }

    public void switchPane(StackPane stackPane, Parent parent) {
        if (!stackPane.getChildren().isEmpty()) {
            savePane = (Parent) stackPane.getChildren().removeLast();
        }
        stackPane.getChildren().setAll(parent);
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    public FXMLLoader setFXMLLoaders() {
        return fxmlLoaders;
    }

    public void close() {
        stage.close();
    }
}
