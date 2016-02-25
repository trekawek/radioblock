package eu.rekawek.radioblock.standalone;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rekawek.radioblock.MutingPipe;
import eu.rekawek.radioblock.Rate;

public class Player {

    private static final Logger LOG = LoggerFactory.getLogger(Player.class);

    private static final String URL = "http://stream3.polskieradio.pl:8904/;stream";

    private final MutingPipe pipe;

    private SourceDataLine line;

    private IceStreamReader reader;

    private Thread playerThread;

    public Player() throws IOException, LineUnavailableException {
        this.pipe = new MutingPipe(Rate.RATE_44_1);
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

        OutputStream os = new AudioOutputStream(line);
        pipe.copyStream(pis, os);
    }
}
