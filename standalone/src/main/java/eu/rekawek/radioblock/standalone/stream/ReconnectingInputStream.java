package eu.rekawek.radioblock.standalone.stream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;

public class ReconnectingInputStream extends RadioStream {

    private static final Logger LOG = LoggerFactory.getLogger(ReconnectingInputStream.class);

    private final RadioStreamSupplier streamSupplier;

    private final int frameSize;

    private final AudioFormat format;

    private boolean framePadding;

    private int frameIndex;

    private RadioStream stream;

    public ReconnectingInputStream(RadioStreamSupplier streamSupplier) throws IOException {
        this.streamSupplier = streamSupplier;
        this.stream = streamSupplier.get();
        this.format = stream.getAudioFormat();
        this.frameSize = format.getFrameSize();
    }

    @Override
    public int available() {
        while (true) {
            try {
                return stream.available();
            } catch (IOException e) {
                LOG.warn("Can't invoke available()", e);
            }
            reconnect();
        }
    }

    @Override
    public int read() {
        while (true) {
            if (framePadding) {
                if (frameIndex++ < frameSize) {
                    return 0;
                } else {
                    framePadding = false;
                    frameIndex = 0;
                }
            }
            try {
                int res = stream.read();
                if (res > -1) {
                    onRead(1);
                    return res;
                }
            } catch (IOException e) {
                LOG.warn("Can't invoke read()", e);
            }
            reconnect();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) {
        while (true) {
            if (framePadding) {
                if (frameIndex < frameSize) {
                    int bytesToSend = frameSize - frameIndex;
                    bytesToSend = Math.min(bytesToSend, len);
                    for (int i = 0; i < bytesToSend; i++) {
                        b[off + i] = 0;
                    }
                    frameIndex += bytesToSend;
                    return bytesToSend;
                } else {
                    framePadding = false;
                    frameIndex = 0;
                }
            }
            try {
                int res = stream.read(b, off, len);
                if (res > -1) {
                    onRead(res);
                    return res;
                }
            } catch (IOException e) {
                LOG.warn("Can't invoke read(byte[], int, int)", e);
            }
            reconnect();
        }
    }

    @Override
    public int read(byte[] b) {
        return read(b, 0, b.length);
    }

    private void onRead(int bytes) {
        frameIndex += bytes;
        frameIndex %= frameSize;
    }

    @Override
    public void close() {
        if (stream != null) {
            IOUtils.closeQuietly(stream);
            stream = null;
        }
    }

    @Override
    public AudioFormat getAudioFormat() {
        return format;
    }

    private void reconnect() {
        framePadding = true;
        close();
        while (stream == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
            try {
                stream = streamSupplier.get();
            } catch (IOException e) {
                LOG.warn("Can't reconnect", e);
            }
        }
    }

    @FunctionalInterface
    public interface RadioStreamSupplier {
        RadioStream get() throws IOException;
    }
}
