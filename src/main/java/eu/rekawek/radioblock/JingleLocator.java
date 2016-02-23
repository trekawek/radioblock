package eu.rekawek.radioblock;

import static java.lang.Math.ceil;
import static java.lang.Math.log;
import static java.lang.Math.pow;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rekawek.radioblock.BufferedAudioAnalyzer.Listener;

public class JingleLocator implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(JingleLocator.class);

    private final List<AudioSample> jingles;

    private final int next2Pow;

    private final int windowSize;

    private final int maxSampleSize;

    private final BlockingQueue<AudioSample> samples = new ArrayBlockingQueue<AudioSample>(2);

    private final List<JingleListener> listeners = new CopyOnWriteArrayList<JingleListener>();

    private volatile boolean running;

    private int jingleIndex;

    public JingleLocator(final List<InputStream> jingleStreams) throws IOException {
        List<byte[]> jingleBuffers = new ArrayList<byte[]>();
        for (InputStream is : jingleStreams) {
            jingleBuffers.add(IOUtils.toByteArray(is));
        }

        int maxSampleSize = 0;
        for (byte[] b : jingleBuffers) {
            if (maxSampleSize < b.length / 4) {
                maxSampleSize = (int) (b.length / 4);
            }
        }
        int windowSize = (int) (maxSampleSize * 1.5);
        int next2Pow = (int) next2Pow(windowSize * 2 - 1);

        this.jingles = new ArrayList<AudioSample>(jingleStreams.size());
        for (byte[] b : jingleBuffers) {
            AudioSample sample = AudioSample.fromBuffer((int) (b.length / 4) + windowSize - 1, b, next2Pow);
            sample.doFft();
            jingles.add(sample);
        }
        this.windowSize = windowSize;
        this.maxSampleSize = maxSampleSize;
        this.next2Pow = next2Pow;
    }

    public void addListener(JingleListener listener) {
        listeners.add(listener);
    }

    public void analyse(InputStream is) {
        BufferedAudioAnalyzer analyzer = new BufferedAudioAnalyzer(is, new Listener() {
            @Override
            public void windowFull(Iterable<Short> window) {
                try {
                    samples.put(new AudioSample(0, windowSize, window, next2Pow));
                } catch (InterruptedException e) {
                    LOG.error("Interrupted put operation", e);
                }
            }
        }, windowSize, windowSize - maxSampleSize);

        running = true;
        Thread t = new Thread(this);
        t.start();

        analyzer.run();

        running = false;
        try {
            t.join();
        } catch (InterruptedException e) {
            LOG.error("Interrupted join", e);
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                AudioSample sample = samples.poll(10, TimeUnit.MILLISECONDS);
                if (sample == null) {
                    continue;
                }
                handleNewWindow(sample);
            } catch (InterruptedException e) {
                LOG.error("Interrupted", e);
                break;
            }
        }
    }

    public void handleNewWindow(AudioSample windowSample) {
        windowSample.doFft();
        windowSample.doConjAndMultiply(jingles.get(jingleIndex));
        windowSample.doIfft();
        float result = windowSample.getMaxReal(2 * windowSize + 1);
        LOG.debug("Result: {}", result);

        if (result >= 150) {
            LOG.debug("Found jingle: {}", jingleIndex);
            for (JingleListener listener : listeners) {
                listener.gotJingle(jingleIndex);
            }

            jingleIndex++;
            jingleIndex = jingleIndex % jingles.size();
        }
    }

    public static long next2Pow(long x) {
        double exp = ceil(log(x) / log(2));
        return (long) pow(2, exp);
    }

    public static interface JingleListener {

        void gotJingle(int index);

    }
}
