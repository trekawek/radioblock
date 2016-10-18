package eu.rekawek.radioblock.standalone;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

public class Main {

    public static void main(String... args) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if (args.length > 0 && "--cli".equals(args[0])) {
            MainCli.main(args);
        }

        final Player player = new Player();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(player);
            }
        });
    }

    private static void createAndShowGUI(Player player) {
        JFrame frame = new JFrame("Radioblock");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        PlayerGui newContentPane = new PlayerGui(player);
        newContentPane.setOpaque(false);
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }

}
