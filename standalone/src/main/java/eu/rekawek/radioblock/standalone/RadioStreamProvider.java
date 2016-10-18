package eu.rekawek.radioblock.standalone;

import javazoom.spi.vorbis.sampled.file.VorbisAudioFileReader;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;

public class RadioStreamProvider {

    public static final String URL = "http://w.dktr.pl:8000/trojka3.ogg";

    public static AudioInputStream getStream() throws IOException, UnsupportedAudioFileException {
        VorbisAudioFileReader vb = new VorbisAudioFileReader();
        AudioInputStream aisOgg = vb.getAudioInputStream(new URL(URL));
        AudioFormat baseFormat = aisOgg.getFormat();
        AudioFormat targetFormat = new AudioFormat(
                PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);
        return AudioSystem.getAudioInputStream(targetFormat, aisOgg);
    }

}
