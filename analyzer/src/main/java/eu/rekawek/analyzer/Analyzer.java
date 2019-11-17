package eu.rekawek.analyzer;

import eu.rekawek.analyzer.channel.MultiplexingStrategy;
import eu.rekawek.analyzer.sample.Sample;
import eu.rekawek.analyzer.sample.WaveformVector;
import eu.rekawek.analyzer.window.BufferedAudioAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.ceil;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.util.stream.Collectors.toList;

public class Analyzer {

    private static final Logger LOG = LoggerFactory.getLogger(Analyzer.class);

    private final int channels;

    private final MultiplexingStrategy multiplexingStrategy;

    private final List<AnalyzerBuilder.Jingle> jingleList;

    private final List<AnalysisListener> listeners = new CopyOnWriteArrayList<>();

    private final List<WaveformVector> waveformVectors;

    private final int maxSampleSize;

    private final int windowSize;

    private final int next2Pow;

    private int jingleIndex;

    Analyzer(AnalyzerBuilder analyzerBuilder) {
        this.channels = analyzerBuilder.getChannels();
        this.multiplexingStrategy = analyzerBuilder.getMultiplexingStrategy();
        this.jingleList = analyzerBuilder.getJingleList();

        List<Sample> samples = jingleList
                .stream()
                .map(AnalyzerBuilder.Jingle::getBuffer)
                .map(b -> new Sample(b, channels))
                .collect(toList());

        maxSampleSize = samples
                .stream()
                .map(Sample::getSize)
                .max(Comparator.naturalOrder()).orElse(0);
        windowSize = (int) (maxSampleSize * 1.5);
        next2Pow = (int) next2Pow(windowSize * 2 - 1);

        waveformVectors = samples
                .stream()
                .map(s -> s.setPadding(s.getSize() + windowSize - 1, next2Pow))
                .map(s -> s.getWaveformVector(multiplexingStrategy))
                .map(WaveformVector::doFft)
                .collect(toList());
    }

    public void addListener(AnalysisListener listener) {
        this.listeners.add(listener);
    }

    public void analyze(InputStream stream) {
        AnalyzerRunnable runnable = new AnalyzerRunnable();

        BufferedAudioAnalyzer analyzer = new BufferedAudioAnalyzer(channels, stream, window -> {
            try {
                runnable.samples.put(multiplexingStrategy.getWaveformVector(window, channels, next2Pow));
            } catch (InterruptedException e) {
                LOG.error("Interrupted put operation", e);
            }
        }, windowSize, windowSize - maxSampleSize);

        Thread t = new Thread(runnable);
        t.start();
        analyzer.run();
        runnable.doStop.set(true);
        LOG.info("Analyzer has finished");

        try {
            t.join();
        } catch (InterruptedException e) {
            LOG.error("Interrupted join", e);
        }
    }

    public void setThreshold(String id, int threshold) {
        jingleList.stream().filter(j -> id.equals(j.getId())).forEach(j -> j.setThreshold(threshold));
    }

    private static long next2Pow(long x) {
        double exp = ceil(log(x) / log(2));
        return (long) pow(2, exp);
    }

    public synchronized void setNextJingleIndex(int index) {
        this.jingleIndex = index;
    }

    private synchronized int getNextJingleIndex() {
        return jingleIndex;
    }

    private class AnalyzerRunnable implements Runnable {

        private final BlockingQueue<WaveformVector> samples = new ArrayBlockingQueue<WaveformVector>(2);

        private AtomicBoolean doStop = new AtomicBoolean();

        private int[] previousResult;

        @Override
        public void run() {
            while (!doStop.get()) {
                try {
                    WaveformVector sample = samples.poll(10, TimeUnit.MILLISECONDS);
                    if (sample == null) {
                        continue;
                    }

                    int localJingleIndex = getNextJingleIndex();
                    boolean found = handleNewWindow(sample, localJingleIndex);
                    if (found) {
                        localJingleIndex++;
                        localJingleIndex %= jingleList.size();
                        setNextJingleIndex(localJingleIndex);
                    }
                } catch (InterruptedException e) {
                    LOG.error("Interrupted", e);
                    break;
                }
            }
        }

        public boolean handleNewWindow(WaveformVector windowSample, int jingleIndex) {
            windowSample.doFft();
            windowSample.doConjAndMultiply(waveformVectors.get(jingleIndex));
            windowSample.doIfft();
            int[] result = toIntArray(windowSample.getMaxReal(2 * windowSize + 1));
            int[] sum;
            if (previousResult == null) {
                sum = result;
            } else {
                sum = sum(result, previousResult);
            }
            int level = IntStream.of(sum).sum();

            previousResult = result;
            int threshold = jingleList.get(jingleIndex).getThreshold();
            if (level > threshold / 2) {
                LOG.info("Result: {}", sum);
            } else {
                LOG.debug("Result: {}", sum);
            }

            String id = jingleList.get(jingleIndex).getId();
            listeners.forEach(l -> l.analysisInProgress(id, jingleIndex, sum));
            if (level >= threshold) {
                LOG.info("Found jingle: {}", id);
                listeners.forEach(l -> l.gotJingle(id, jingleIndex, sum));
                previousResult = null;
                return true;
            } else {
                return false;
            }
        }

        private int[] toIntArray(Float[] array) {
            int[] result = new int[array.length];
            for (int i = 0; i < array.length; i++) {
                result[i] = (int) ((float) array[i]);
            }
            return result;
        }

        private int[] sum(int[] array1, int[] array2) {
            checkArgument(array1.length == array2.length);
            int[] s = new int[array1.length];
            for (int i = 0; i < array1.length; i++) {
                s[i] = array1[i] + array2[i];
            }
            return s;
        }
    }
}
