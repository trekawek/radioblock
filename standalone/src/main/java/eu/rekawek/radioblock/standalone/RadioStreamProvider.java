package eu.rekawek.radioblock.standalone;

import javazoom.spi.vorbis.sampled.file.VorbisAudioFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;

public class RadioStreamProvider {

    private static final Logger LOG = LoggerFactory.getLogger(RadioStreamProvider.class);

    public static RadioStream getStream() {
        try {
            return getVorbis("http://w.dktr.pl:8000/trojka3.ogg");
        } catch (IOException | UnsupportedAudioFileException e) {
            LOG.error("Can't get the w.dktr.pl stream", e);
        }
        try {
            return getIcecast("http://stream3.polskieradio.pl:8904/;stream", 96000);
        } catch (IOException e) {
            LOG.error("Can't get the official stream", e);
        }
        return null;
    }

    public static RadioStream getVorbis(String url) throws IOException, UnsupportedAudioFileException {
        VorbisAudioFileReader vb = new VorbisAudioFileReader();
        AudioInputStream aisOgg = vb.getAudioInputStream(new URL(url));
        AudioFormat baseFormat = aisOgg.getFormat();
        AudioFormat targetFormat = new AudioFormat(
                PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);
        AudioInputStream result = AudioSystem.getAudioInputStream(targetFormat, aisOgg);
        return new RadioStream(result, result.getFormat());
    }

    public static RadioStream getIcecast(String url, int bitrate) throws IOException {
        AudioFormat pcm = new AudioFormat(44100, 16, 2, true, false);
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);
        IceStreamReader reader = new IceStreamReader(new URL(url), bitrate, pos);
        new Thread(reader).start();
        return new RadioStream(pis, pcm);
    }

    public static class RadioStream {

        private final InputStream stream;

        private final AudioFormat format;

        public RadioStream(InputStream stream, AudioFormat format) {
            this.stream = stream;
            this.format = format;
        }


        public InputStream getStream() {
            return stream;
        }

        public AudioFormat getFormat() {
            return format;
        }
    }
}
