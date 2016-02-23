package eu.rekawek.radioblock;

import java.nio.ShortBuffer;
import java.util.Iterator;

public class PcmBufferIterator implements Iterator<Short> {

    private final ShortBuffer shortBuffer;

    public PcmBufferIterator(ShortBuffer shortBuffer) {
        this.shortBuffer = shortBuffer;
    }

    @Override
    public boolean hasNext() {
        return shortBuffer.hasRemaining();
    }

    @Override
    public Short next() {
        short val = shortBuffer.get();
        shortBuffer.get();
        return val;
    }
}