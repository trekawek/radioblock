package eu.rekawek.analyzer.channel;

import com.google.common.collect.AbstractIterator;

import java.util.Iterator;
import java.util.stream.IntStream;

public class CombinedChannelIterator extends AbstractIterator<Short> {

    private final Iterator<Short> it;

    private final int[] localBuffer;

    public CombinedChannelIterator(Iterator<Short> it, int channels) {
        this.it = it;
        this.localBuffer = new int[channels];
    }

    @Override
    protected Short computeNext() {
        for (int i = 0; i < localBuffer.length; i++) {
            if (it.hasNext()) {
                localBuffer[i] = it.next();
            } else {
                return endOfData();
            }
        }
        return (short) IntStream.of(localBuffer).average().orElse(0);
    }
}
