package eu.rekawek.analyzer.sample;

import com.google.common.collect.Iterators;
import eu.rekawek.analyzer.channel.MultiplexingStrategy;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.Iterator;

import static com.google.common.collect.Iterators.cycle;
import static com.google.common.collect.Iterators.limit;

public class Sample {

    private final byte[] buffer;

    private final int channels;

    private final int size;

    private Integer prePadding;

    private Integer minSize;

    public Sample(byte[] buffer, int channels) {
        this.buffer = buffer;
        this.channels = channels;
        this.size = buffer.length / (2 * channels);
    }

    public Sample setPadding(int prePadding, int minSize) {
        this.prePadding = prePadding;
        this.minSize = minSize;
        return this;
    }

    public WaveformVector getWaveformVector(MultiplexingStrategy strategy) {
        int finalSize = size;

        final Iterator<Short> prePaddingIt;
        if (prePadding != null && finalSize < prePadding) {
            prePaddingIt = limit(cycle((short) 0), (prePadding - finalSize) * channels);
            finalSize = prePadding;
        } else {
            prePaddingIt = Collections.emptyIterator();
        }
        finalSize = Math.max(finalSize, minSize);

        Iterator<Short> it = Iterators.concat(prePaddingIt, asShortIterator());
        return strategy.getWaveformVector(it, channels, finalSize);
    }

    private Iterator<Short> asShortIterator() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return new ShortBufferIterator(byteBuffer.asShortBuffer());
    }

    public int getSize() {
        return size;
    }

    public static class ShortBufferIterator implements Iterator<Short> {

        private final ShortBuffer buffer;

        public ShortBufferIterator(ShortBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public boolean hasNext() {
            return buffer.hasRemaining();
        }

        @Override
        public Short next() {
            return buffer.get();
        }
    }
}
