package eu.rekawek.radioblock.standalone;

import java.awt.*;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Main {

    public static void main(String... args) throws IOException, LineUnavailableException, UnsupportedAudioFileException, AWTException {
        final PlayerPrefs prefs = new PlayerPrefs();
        if (!prefs.isShowWindow()) {
            System.setProperty("apple.awt.UIElement", "true");
        }

        javax.swing.SwingUtilities.invokeLater(() -> createAndShowGUI(prefs));
    }

    private static void createAndShowGUI(PlayerPrefs prefs) {
        try {
            new PlayerController(prefs);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
