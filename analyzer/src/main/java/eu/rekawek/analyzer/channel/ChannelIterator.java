package eu.rekawek.analyzer.channel;

import com.google.common.collect.AbstractIterator;

import java.util.Iterator;

public class ChannelIterator extends AbstractIterator<Short> {

    private final Iterator<Short> it;

    private final int channelIndex;

    private final short[] localBuffer;

    public ChannelIterator(Iterator<Short> it, int channels, int channelIndex) {
        this.it = it;
        this.channelIndex = channelIndex;
        this.localBuffer = new short[channels];
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
        return localBuffer[channelIndex];
    }
}
