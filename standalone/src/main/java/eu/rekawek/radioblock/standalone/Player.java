package eu.rekawek.radioblock.standalone;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.io.input.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rekawek.radioblock.JingleLocator;
import eu.rekawek.radioblock.JingleLocator.JingleListener;

public class Player {

    private static final Logger LOG = LoggerFactory.getLogger(Player.class);

    private static final String URL = "http://stream3.polskieradio.pl:8904/;stream";

    private final JingleLocator locator;

    private SourceDataLine line;

    private IceStreamReader reader;

    private Thread playerThread;

    public Player() throws IOException, LineUnavailableException {
        List<InputStream> jingles = new ArrayList<InputStream>();
        for (String name : asList("commercial-start-44.1k.raw", "commercial-end-44.1k.raw")) {
            jingles.add(Main.class.getClassLoader().getResourceAsStream(name));
        }
        locator = new JingleLocator(jingles, 200);
    }

    public synchronized void start() {
        if (playerThread != null && playerThread.isAlive()) {
            return;
        }
        playerThread = new Thread(new Runnable() {
            public void run() {
                try {
                    doStart();
                } catch (Exception e) {
                    LOG.error("Can't start player", e);
                }
            }
        });
        playerThread.start();
    }

    public synchronized void stop() {
        if (playerThread != null && playerThread.isAlive()) {
            reader.stop();
            line.stop();
        }
    }

    private void doStart() throws LineUnavailableException, IOException {
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);

        reader = new IceStreamReader(new URL(URL), 96000, pos);
        new Thread(reader).start();

        AudioFormat pcm = new AudioFormat(44100, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, pcm);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(pcm);
        line.start();

        final MutableOutputStream mos = new MutableOutputStream(new AudioOutputStream(line));
        locator.addListener(new JingleListener() {
            @Override
            public void gotJingle(int index, float level) {
                if (index == 0) {
                    mos.setVolumeLevel(0.1f);
                } else {
                    mos.setVolumeLevel(1);
                }
            }
        });
        TeeInputStream tis = new TeeInputStream(pis, mos);
        locator.analyse(tis);
    }
}
