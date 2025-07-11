package org.example.chessgame.Sound;

import javafx.scene.media.AudioClip;

public class GameSounds {
    public final AudioClip captureSound;
    public final AudioClip moveSelfSound;
    public final AudioClip castleSound;
    public final AudioClip moveCheckSound;
    public final AudioClip promotionSound;
    public final AudioClip premoveSound;
    public final AudioClip gameEndSound;

    public GameSounds() {
        captureSound = new AudioClip(getClass().getResource("GameSounds/capture.wav").toExternalForm());
        moveSelfSound = new AudioClip(getClass().getResource("GameSounds/move-self.wav").toExternalForm());
        castleSound = new AudioClip(getClass().getResource("GameSounds/castle.wav").toExternalForm());
        moveCheckSound = new AudioClip(getClass().getResource("GameSounds/move-check.wav").toExternalForm());
        promotionSound = new AudioClip(getClass().getResource("GameSounds/promote.wav").toExternalForm());
        premoveSound = new AudioClip(getClass().getResource("GameSounds/premove.wav").toExternalForm());
        gameEndSound = new AudioClip(getClass().getResource("GameSounds/game-end.wav").toExternalForm());
    }

    public void setVolume(double volume) {
        volume /= 100;

        captureSound.setVolume(volume);
        moveSelfSound.setVolume(volume);
        castleSound.setVolume(volume);
        moveCheckSound.setVolume(volume);
        promotionSound.setVolume(volume);
        premoveSound.setVolume(volume);
        gameEndSound.setVolume(volume);
    }
}