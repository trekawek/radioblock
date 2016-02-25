package eu.rekawek.radioblock;

import java.nio.ShortBuffer;
import java.util.Iterator;

public class PcmBufferIterator implements Iterator<Short> {

    private final ShortBuffer shortBuffer;

    private final int channels;

    public PcmBufferIterator(int channels, ShortBuffer shortBuffer) {
        this.shortBuffer = shortBuffer;
        this.channels = channels;
    }

    @Override
    public boolean hasNext() {
        return shortBuffer.hasRemaining();
    }

    @Override
    public Short next() {
        short val = shortBuffer.get();
        if (channels == 2) {
            shortBuffer.get(); // drop the right channel for stereo
        }
        return val;
    }
}