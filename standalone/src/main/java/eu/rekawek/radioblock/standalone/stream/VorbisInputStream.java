package eu.rekawek.radioblock.standalone.stream;

import javazoom.spi.vorbis.sampled.file.VorbisAudioFileReader;
import org.apache.commons.io.IOUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;

public class VorbisInputStream extends RadioStream {

    private final AudioInputStream delegate;

    public VorbisInputStream(String url) throws IOException {
        VorbisAudioFileReader vb = new VorbisAudioFileReader();
        AudioInputStream aisOgg;
        try {
            aisOgg = vb.getAudioInputStream(new URL(url));
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
        }
        AudioFormat baseFormat = aisOgg.getFormat();
        AudioFormat targetFormat = new AudioFormat(
                PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);
        try {
            delegate = AudioSystem.getAudioInputStream(targetFormat, aisOgg);
        } catch (IllegalArgumentException e) {
            IOUtils.closeQuietly(aisOgg);
            throw new IOException(e);
        }
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return delegate.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return delegate.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public AudioFormat getAudioFormat() {
        return delegate.getFormat();
    }
}
