package eu.rekawek.radioblock.standalone;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import eu.rekawek.analyzer.AnalysisListener;
import eu.rekawek.radioblock.standalone.stream.RadioStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Player {

    private static final Logger LOG = LoggerFactory.getLogger(Player.class);

    private final List<AnalysisListener> listeners = new ArrayList<>();

    private MutingPipe pipe;

    private SourceDataLine line;

    private Thread playerThread;

    private InputStream radioStream;

    private int[] thresholds = new int[2];

    public Player(PlayerPrefs prefs) {
        thresholds[0] = prefs.getOpeningThreshold();
        thresholds[1] = prefs.getClosingThreshold();
    }

    public void setThreshold(int jingleIndex, int newLevel) {
        thresholds[jingleIndex] = newLevel;
        if (pipe != null) {
            pipe.setThreshold(jingleIndex, newLevel);
        }
    }

    public synchronized void start(Runnable errorCallback) {
        if (playerThread != null && playerThread.isAlive()) {
            errorCallback.run();
            return;
        }
        playerThread = new Thread(() -> {
            try {
                while (true) {
                    RadioStream rs = RadioStreamProvider.getStream();
                    if (rs == null) {
                        errorCallback.run();
                        return;
                    }
                    doStart(rs);
                }
            } catch (Exception e) {
                LOG.error("Can't start player", e);
            }
        });
        playerThread.start();
    }

    public synchronized void stop() {
        if (playerThread != null && playerThread.isAlive()) {
            if (line != null) {
                line.stop();
                line = null;
            }
            try {
                if (radioStream != null) {
                    radioStream.close();
                    radioStream = null;
                }
            } catch (IOException e) {
                LOG.error("Can't close OGG input stream", e);
            }
            playerThread = null;
        }
    }

    public void addListener(AnalysisListener listener) {
        listeners.add(listener);
    }

    private void doStart(RadioStream radioStream) throws LineUnavailableException, IOException {
        if (radioStream == null) {
            return;
        }
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, radioStream.getAudioFormat());
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(radioStream.getAudioFormat());
        line.start();

        try {
            OutputStream os = new AudioOutputStream(line);

            pipe = new MutingPipe(radioStream.getAudioFormat(), thresholds[0], thresholds[1]);
            listeners.forEach(pipe::addListener);
            pipe.copyStream(radioStream, os);
        } finally {
            pipe.close();
            line.stop();
            line.close();
            radioStream.close();
        }
    }

}
