package eu.rekawek.analyzer.channel;

import java.util.Iterator;

public class ChannelIterator implements Iterator<Short> {

    private final Iterator<Short> it;

    private final int channelIndex;

    private final short[] localBuffer;

    public ChannelIterator(Iterator<Short> it, int channels, int channelIndex) {
        this.it = it;
        this.channelIndex = channelIndex;
        this.localBuffer = new short[channels];
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
        return localBuffer[channelIndex];
    }
}
