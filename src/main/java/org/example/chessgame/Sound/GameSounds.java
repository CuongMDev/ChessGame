package org.example.chessgame.Sound;

import javax.sound.sampled.AudioInputStream;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class GameSounds {
    public final Clip captureSound;
    public final Clip moveSelfSound;
    public final Clip castleSound;
    public final Clip moveCheckSound;
    public final Clip promotionSound;
    public final Clip premoveSound;
    public final Clip gameEndSound;

    public GameSounds() {
        captureSound = loadClip("GameSounds/capture.wav");
        moveSelfSound = loadClip("GameSounds/move-self.wav");
        castleSound = loadClip("GameSounds/castle.wav");
        moveCheckSound = loadClip("GameSounds/move-check.wav");
        promotionSound = loadClip("GameSounds/promote.wav");
        premoveSound = loadClip("GameSounds/premove.wav");
        gameEndSound = loadClip("GameSounds/game-end.wav");
    }

    private static Clip loadClip(String resourcePath) {
        try {
            URL url = GameSounds.class.getResource(resourcePath);
            if (url == null) {
                System.err.println("File not found: " + resourcePath);
                return null;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void play(Clip clip) {
        if (clip != null) {
            if (clip.isRunning()) clip.stop(); // tránh xung đột
            clip.setFramePosition(0);          // quay về đầu
            clip.start();
        }
    }
}