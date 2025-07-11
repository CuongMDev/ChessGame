package org.example.chessgame.Menu.Setting;

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
    public Button okButton;

    public ConfigLoader config;

    private void loadSettings() {
        Double thinkingAbility = config.getDouble("ThinkingAbility");
        Double gameMusic = config.getDouble("GameMusic");

        if (thinkingAbility == null) {
            thinkingAbility = 2.0;
        }
        if (gameMusic == null) {
            gameMusic = 100.0;
        }

        thinkingAbilitySlider.setValue(thinkingAbility);
        gameMusicSlider.setValue(gameMusic);
    }

    public void saveSettings() {
        config.set("ThinkingAbility", String.valueOf(thinkingAbilitySlider.getValue()));
        config.set("GameMusic", String.valueOf(gameMusicSlider.getValue()));
        config.save();
    }

    private void initSlider() {
        thinkingAbilitySlider.setMin(1);
        thinkingAbilitySlider.setSnapToTicks(true);
        thinkingAbilitySlider.setMajorTickUnit(1);
        thinkingAbilitySlider.setMinorTickCount(0);

        gameMusicSlider.setSnapToTicks(true);
        gameMusicSlider.setMajorTickUnit(1);
        gameMusicSlider.setMinorTickCount(0);
    }

    @FXML
    private void initialize() {
        config = new ConfigLoader();

        initSlider();
        loadSettings();
    }
}
