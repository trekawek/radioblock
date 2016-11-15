package eu.rekawek.analyzer.channel;

import java.util.Iterator;
import java.util.stream.IntStream;

public class CombinedChannelIterator implements Iterator<Short> {

    private final Iterator<Short> it;

    private final int[] localBuffer;

    public CombinedChannelIterator(Iterator<Short> it, int channels) {
        this.it = it;
        this.localBuffer = new int[channels];
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public Short next() {
        for (int i = 0; i < localBuffer.length; i++) {
            localBuffer[i] = it.next();
        }
        return (short) IntStream.of(localBuffer).average().orElse(0);
    }
}
