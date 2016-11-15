package eu.rekawek.analyzer.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class FuseInputStream extends InputStream {

    private static final Logger LOG = LoggerFactory.getLogger(FuseInputStream.class);

    private final InputStream is;

    private volatile boolean alive;

    public FuseInputStream(InputStream is, final long timeoutMillis, final Runnable callback) {
        this.is = is;
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    alive = false;
                    Thread.sleep(timeoutMillis);
                    if (!alive) {
                        LOG.info("No activity for {}ms", timeoutMillis);
                        callback.run();
                        break;
                    }
                } catch (InterruptedException e) {
                    LOG.error("Interrupted fuse loop", e);
                    break;
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public int read() throws IOException {
        int b = is.read();
        alive = true;
        return b;
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}
