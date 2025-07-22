package org.example.chessgame.Menu.Setting;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import org.example.chessgame.Abstract.Controller;

public class SettingController extends Controller {
    @FXML
    public Slider thinkingAbilitySlider;
    @FXML
    public Slider gameMusicSlider;
    @FXML
    public Slider searchThreadSlider;

    @FXML
    public Button okButton;

    public ConfigLoader config;

    private void loadSettings() {
        Double thinkingAbility = config.getDouble("ThinkingAbility");
        Double gameMusic = config.getDouble("GameMusic");
        Double searchThread = config.getDouble("SearchThread");

        if (thinkingAbility == null) {
            thinkingAbility = 2.0;
        }
        if (gameMusic == null) {
            gameMusic = 100.0;
        }
        if (searchThread == null) {
            searchThread = 32.0;
        }

        thinkingAbilitySlider.setValue(thinkingAbility);
        gameMusicSlider.setValue(gameMusic);
        searchThreadSlider.setValue(searchThread);
    }

    public void saveSettings() {
        config.set("ThinkingAbility", String.valueOf(thinkingAbilitySlider.getValue()));
        config.set("GameMusic", String.valueOf(gameMusicSlider.getValue()));
        config.set("SearchThread", String.valueOf(searchThreadSlider.getValue()));
        config.save();
    }

    private void initSlider() {
        thinkingAbilitySlider.setMin(1);
        thinkingAbilitySlider.setSnapToTicks(true);
        thinkingAbilitySlider.setMajorTickUnit(1);
        thinkingAbilitySlider.setMinorTickCount(0);
        thinkingAbilitySlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                Platform.runLater(() -> {
                    searchThreadSlider.setValue(Double.min(searchThreadSlider.getValue(), thinkingAbilitySlider.getValue() * 100));
                });
            }
        });

        gameMusicSlider.setSnapToTicks(true);
        gameMusicSlider.setMajorTickUnit(1);
        gameMusicSlider.setMinorTickCount(0);

        searchThreadSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                Platform.runLater(() -> {
                    searchThreadSlider.setValue(Double.min(searchThreadSlider.getValue(), thinkingAbilitySlider.getValue() * 100));
                });
            }
        });
        searchThreadSlider.setMin(1);
        searchThreadSlider.setMax(800);
        searchThreadSlider.setSnapToTicks(true);
        searchThreadSlider.setMajorTickUnit(1);
        searchThreadSlider.setMinorTickCount(0);
    }

    @FXML
    private void initialize() {
        config = new ConfigLoader();

        initSlider();
        loadSettings();
    }
}
