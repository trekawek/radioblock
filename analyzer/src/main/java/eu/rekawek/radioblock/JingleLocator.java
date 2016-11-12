package eu.rekawek.radioblock;

import static java.lang.Math.ceil;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    private final List<Integer> thresholds;

    private final int channels;

    private volatile boolean running;

    private int jingleIndex;

    private float previousResult;

    public JingleLocator(final List<InputStream> jingleStreams, final List<Integer> thresholds, int channels) throws IOException {
        this.thresholds = Collections.synchronizedList(new ArrayList<>(thresholds));
        this.channels = channels;

        List<byte[]> jingleBuffers = jingleStreams.stream().map(JingleLocator::toByteArray).collect(toList());

        final int bytesPerSample = channels * 2;
        int maxSampleSize = jingleBuffers.stream().map(b -> b.length / bytesPerSample).max(Comparator.naturalOrder()).orElse(0);
        int windowSize = (int) (maxSampleSize * 1.5);
        int next2Pow = (int) next2Pow(windowSize * 2 - 1);

        this.jingles = jingleBuffers.stream()
                .map(b -> AudioSample.fromBuffer(channels, b.length / bytesPerSample + windowSize - 1, b, next2Pow))
                .map(AudioSample::doFft)
                .collect(toList());

        this.windowSize = windowSize;
        this.maxSampleSize = maxSampleSize;
        this.next2Pow = next2Pow;
    }

    public void addListener(JingleListener listener) {
        listeners.add(listener);
    }

    public void analyse(InputStream is) {
        BufferedAudioAnalyzer analyzer = new BufferedAudioAnalyzer(channels, is, window -> {
            try {
                samples.put(new AudioSample(0, windowSize, window, next2Pow));
            } catch (InterruptedException e) {
                LOG.error("Interrupted put operation", e);
            }
        }, windowSize, windowSize - maxSampleSize);

        running = true;
        Thread t = new Thread(this);
        t.start();

        analyzer.run();
        LOG.info("Analyzer has finished");
        running = false;

        try {
            t.join();
        } catch (InterruptedException e) {
            LOG.error("Interrupted join", e);
        }
    }

    @Override
    public void run() {
        jingleIndex = 0;
        previousResult = 0;
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
        float sum = result + previousResult;
        previousResult = result;

        if (sum > thresholds.get(jingleIndex) / 2) {
            LOG.info("Result: {}", sum);
        } else {
            LOG.debug("Result: {}", sum);
        }

        listeners.forEach(l -> l.windowUpdated(jingleIndex, sum));
        if (sum >= thresholds.get(jingleIndex)) {
            LOG.info("Found jingle: {}", jingleIndex);
            listeners.forEach(l -> l.gotJingle(jingleIndex, sum));
            jingleIndex++;
            jingleIndex = jingleIndex % jingles.size();
            previousResult = 0;
        }
    }

    public static long next2Pow(long x) {
        double exp = ceil(log(x) / log(2));
        return (long) pow(2, exp);
    }

    private static byte[] toByteArray(InputStream is) {
        try {
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void setThreshold(int jingleIndex, int newLevel) {
        thresholds.set(jingleIndex, newLevel);
    }

    public interface JingleListener {

        void gotJingle(int index, float level);

        default void windowUpdated(int index, float level) {
        }
    }
}
