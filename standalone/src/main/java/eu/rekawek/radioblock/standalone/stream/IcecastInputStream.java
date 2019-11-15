package eu.rekawek.radioblock.standalone.stream;

import org.apache.commons.io.IOUtils;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;

public class IcecastInputStream extends RadioStream {

    private final AudioFormat pcm;

    private final PipedInputStream pis;

    private final Thread pumpThread;

    private final IcecastStreamReader reader;

    public IcecastInputStream(String url) throws IOException {
        pcm = new AudioFormat(44100, 16, 2, true, false);
        pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);
        reader = new IcecastStreamReader(new URL(url), 96000, pos);
        pumpThread = new Thread(reader);
        pumpThread.start();
    }

    @Override
    public int available() throws IOException {
        return pis.available();
    }

    @Override
    public int read() throws IOException {
        return pis.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return pis.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return pis.read(b, off, len);
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(reader);
        IOUtils.closeQuietly(pis);
    }

    @Override
    public AudioFormat getAudioFormat() {
        return pcm;
    }
}
