package eu.rekawek.radioblock.standalone;

import java.awt.*;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

public class Main {

    public static final String SHOW_WINDOW_PROP = "showWindow";

    public static void main(String... args) throws IOException, LineUnavailableException, UnsupportedAudioFileException, AWTException {
        if (args.length > 0 && "--cli".equals(args[0])) {
            MainCli.main(args);
            return;
        }

        final Player player = new Player();
        final Preferences prefs = Preferences.userRoot().node(Main.class.getName());
        if (!prefs.getBoolean(SHOW_WINDOW_PROP, true)) {
            System.setProperty("apple.awt.UIElement", "true");
        }

        javax.swing.SwingUtilities.invokeLater(() -> createAndShowGUI(player, prefs));
    }

    private static void createAndShowGUI(Player player, Preferences prefs) {
        JFrame frame = new JFrame("Radioblock");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        PlayerGui newContentPane = new PlayerGui(player, prefs);
        newContentPane.setOpaque(false);
        frame.setContentPane(newContentPane);

        frame.setMinimumSize(new Dimension(300, 0));
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(prefs.getBoolean(SHOW_WINDOW_PROP, true));
    }

}
