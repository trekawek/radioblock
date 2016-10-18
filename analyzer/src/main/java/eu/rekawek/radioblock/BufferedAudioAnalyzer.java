package eu.rekawek.radioblock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferedAudioAnalyzer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BufferedAudioAnalyzer.class);

    private final InputStream input;

    private final byte[] rawBuffer = new byte[4096];

    private final short[] buffer;

    private final int windowSize;

    private final Listener listener;

    private int index;

    private int windowIndex;

    private int channels;

    public BufferedAudioAnalyzer(int channels, InputStream input, Listener listener, int bufferSize, int windowSize) {
        this.input = input;
        this.listener = listener;
        this.buffer = new short[bufferSize];
        this.windowSize = windowSize;
        this.windowIndex = windowSize - bufferSize;
        this.channels = channels;
    }

    private void readLoop() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(2 * channels);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        while (true) {
            int n = input.read(rawBuffer);
            if (n == -1) {
                break;
            }

            for (int i = 0; i < n; i += buf.capacity()) {
                buf.clear();
                buf.put(rawBuffer, i, buf.capacity());
                buf.rewind();
                short val = buf.getShort(); // drop the right channel for stereo
                buffer[index++] = val;
                index = index % buffer.length;

                if (++windowIndex == windowSize) {
                    broadcast();
                    windowIndex = 0;
                }
            }
        }
    }

    private void broadcast() {
        listener.windowFull(new Iterable<Short>() {
            @Override
            public Iterator<Short> iterator() {
                return new WrappedArrayIterator(buffer, index);
            }
        });
    }

    @Override
    public void run() {
        try {
            readLoop();
        } catch (IOException e) {
            LOG.error("Error in the loop", e);
        }
    }

    public interface Listener {
        void windowFull(Iterable<Short> window);
    }
}
