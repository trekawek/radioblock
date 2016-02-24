package eu.rekawek.radioblock.standalone;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.SourceDataLine;

public class AudioOutputStream extends OutputStream {

    private final SourceDataLine line;

    private final byte[] buff = new byte[4];

    private int index = 0;

    AudioOutputStream(SourceDataLine line) {
        this.line = line;
    }

    @Override
    public void write(int b) throws IOException {
        buff[index++] = (byte) b;
        if (index == 4) {
            line.write(buff, 0, 4);
            index = 0;
        }
    }
}