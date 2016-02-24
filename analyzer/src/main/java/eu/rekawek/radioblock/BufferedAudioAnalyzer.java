package eu.rekawek.radioblock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

public class BufferedAudioAnalyzer implements Runnable {

    private final InputStream input;

    private final short[] buffer;

    private final int windowSize;

    private final Listener listener;

    private int index;

    private int windowIndex;

    public BufferedAudioAnalyzer(InputStream input, Listener listener, int bufferSize, int windowSize) {
        this.input = input;
        this.listener = listener;
        this.buffer = new short[bufferSize];
        this.windowSize = windowSize;
        this.windowIndex = windowSize - bufferSize;
    }

    private void readLoop() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        while (true) {
            buf.clear();
            for (int i = 0; i < buf.capacity(); i++) {
                int b = input.read();
                if (b == -1) {
                    return;
                }
                buf.put((byte) b);
            }
            buf.rewind();
            short val = buf.getShort(); // just left channel
            buffer[index++] = val;
            index = index % buffer.length;

            if (++windowIndex == windowSize) {
                broadcast();
                windowIndex = 0;
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
            e.printStackTrace();
        }
    }

    public static interface Listener {
        void windowFull(Iterable<Short> window);
    }
}
