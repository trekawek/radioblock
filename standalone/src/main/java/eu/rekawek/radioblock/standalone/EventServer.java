package eu.rekawek.radioblock.standalone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class EventServer implements Closeable, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(EventServer.class);

    private final String path;

    private OutputStream os;

    private final ByteBuffer buffer = ByteBuffer.allocate(3);

    private volatile short xCorrValue;

    private volatile byte jingleIndex = (byte) 0xff;

    private volatile boolean stop;

    private long lastUpdate;

    public EventServer(String path) {
        this.path = path;
        new Thread(this).start();
    }

    public void close() throws IOException {
        stop = true;
        if (os != null) {
            os.close();
        }
    }

    public void update(short xCorrValue, byte index) {
        this.xCorrValue = xCorrValue;
        this.jingleIndex = index;
    }

    public void writeState() throws IOException {
        buffer.rewind();
        buffer.putShort(xCorrValue);
        buffer.put(jingleIndex);
        os.write(buffer.array());
        os.flush();
    }

    @Override
    public void run() {
        try {
            os = new FileOutputStream(path);
            while (!stop) {
                long now = System.currentTimeMillis();
                if (now - lastUpdate > 100) {
                    writeState();
                    lastUpdate = now;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    LOG.error("Interrupted", e);
                }
            }
        } catch (IOException e) {
            LOG.error("Can't write state", e);
        }
    }
}
