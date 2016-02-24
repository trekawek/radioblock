package eu.rekawek.radioblock.standalone;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.input.TeeInputStream;

import eu.rekawek.radioblock.JingleLocator;
import eu.rekawek.radioblock.MutableOutputStream;
import eu.rekawek.radioblock.MuteStream;
import eu.rekawek.radioblock.JingleLocator.JingleListener;
import javazoom.jl.decoder.JavaLayerException;

public class Main {

    public static void main(String... args)
            throws IOException, UnsupportedAudioFileException, JavaLayerException, LineUnavailableException {
        URL url = new URL("http://stream3.polskieradio.pl:8904/;stream");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);

        IceStreamReader reader = new IceStreamReader(url, 96000, pos);
        new Thread(reader).start();

        List<InputStream> jingles = new ArrayList<InputStream>();
        for (String name : asList("commercial-start-44.1k.raw", "commercial-end-44.1k.raw")) {
            jingles.add(MuteStream.class.getClassLoader().getResourceAsStream(name));
        }
        JingleLocator locator = new JingleLocator(jingles, 200);

        AudioFormat pcm = new AudioFormat(44100, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, pcm);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(pcm);
        line.start();

        final MutableOutputStream mos = new MutableOutputStream(asOutputStream(line));
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

    private static OutputStream asOutputStream(final SourceDataLine line) {
        return new OutputStream() {

            private final byte[] buff = new byte[4];

            private int index = 0;

            @Override
            public void write(int b) throws IOException {
                buff[index++] = (byte) b;
                if (index == 4) {
                    line.write(buff, 0, 4);
                    index = 0;
                }
            }
        };
    }

}
