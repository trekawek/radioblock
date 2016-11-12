package eu.rekawek.radioblock.standalone;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import eu.rekawek.radioblock.JingleLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rekawek.radioblock.Rate;

public class Player {

    private static final Logger LOG = LoggerFactory.getLogger(Player.class);

    private final MutingPipe pipe;

    private SourceDataLine line;

    private Thread playerThread;

    private AudioInputStream radioStream;

    public Player(PlayerPrefs prefs) throws IOException, LineUnavailableException {
        this.pipe = new MutingPipe(Rate.RATE_48, prefs.getOpeningThreshold(), prefs.getClosingThreshold());
    }

    public void setThreshold(int jingleIndex, int newLevel) {
        pipe.setThreshold(jingleIndex, newLevel);
    }

    public synchronized void start() {
        if (playerThread != null && playerThread.isAlive()) {
            return;
        }
        playerThread = new Thread(() -> {
            try {
                doStart();
            } catch (Exception e) {
                LOG.error("Can't start player", e);
            }
        });
        playerThread.start();
    }

    public synchronized void stop() {
        if (playerThread != null && playerThread.isAlive()) {
            line.stop();
            try {
                radioStream.close();
            } catch (IOException e) {
                LOG.error("Can't close OGG input stream", e);
            }
            playerThread = null;
        }
    }

    public void addListener(JingleLocator.JingleListener listener) {
        pipe.addListener(listener);
    }

    private void doStart() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        radioStream = RadioStreamProvider.getStream();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, radioStream.getFormat());
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(radioStream.getFormat());
        line.start();

        OutputStream os = new AudioOutputStream(line);
        pipe.copyStream(radioStream, os);
    }

}
