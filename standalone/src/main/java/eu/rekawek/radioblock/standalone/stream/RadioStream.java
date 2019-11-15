package eu.rekawek.radioblock.standalone.stream;

import javax.sound.sampled.AudioFormat;
import java.io.InputStream;

public abstract class RadioStream extends InputStream {

    public abstract AudioFormat getAudioFormat();

}
