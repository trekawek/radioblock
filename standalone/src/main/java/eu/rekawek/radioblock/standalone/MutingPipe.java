package eu.rekawek.radioblock.standalone;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import eu.rekawek.analyzer.AnalysisListener;
import eu.rekawek.analyzer.Analyzer;
import eu.rekawek.analyzer.AnalyzerBuilder;
import eu.rekawek.analyzer.channel.MultiplexingStrategy;
import org.apache.commons.io.input.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;

public class MutingPipe implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(MutingPipe.class);

    private static final long UNMUTE_AFTER = TimeUnit.MINUTES.toMillis(5);

    private final Analyzer analyzer;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private volatile long lastUpdate = Long.MAX_VALUE;

    private volatile int index = 1;

    private volatile boolean stop;

    private MuteableOutputStream mos;

    public MutingPipe(AudioFormat format, int openingThreshold, int closingThreshold) throws IOException {
        String rate = getRate(format);

        executorService.submit(() -> safeLoop());
        AnalyzerBuilder builder = new AnalyzerBuilder();
        builder.setChannels(2);
        builder.setMultiplexingStrategy(MultiplexingStrategy.AVERAGE);
        builder.addJingle("opening", getJingleStream("start", rate), openingThreshold);
        builder.addJingle("closing", getJingleStream("end", rate), closingThreshold);
        analyzer = builder.build();
        analyzer.addListener((id, jingleIndex, levels) -> onJingle(jingleIndex));
    }

    private synchronized void onJingle(int jingleIndex) {
        if (mos != null) {
            if (jingleIndex == 0) {
                mos.setVolumeLevel(0.01f);
            } else {
                mos.setVolumeLevel(1);
            }
        }
        index = jingleIndex;
        lastUpdate = System.currentTimeMillis();
    }

    public void addListener(AnalysisListener listener) {
        analyzer.addListener(listener);
    }

    public void copyStream(InputStream is, OutputStream os) {
        mos = new MuteableOutputStream(os);
        TeeInputStream tis = new TeeInputStream(is, mos);
        analyzer.analyze(tis);
    }

    public void setThreshold(int jingleIndex, int newLevel) {
        analyzer.setThreshold(jingleIndex == 0 ? "opening" : "closing", newLevel);
    }

    private static String getRate(AudioFormat format) {
        int rate = (int) format.getFrameRate();
        switch (rate) {
            case 44100:
                return "44.1k";
            case 48000:
                return "48k";
            default:
                throw new IllegalArgumentException("Not supported rate: " + rate);
        }
    }

    private static InputStream getJingleStream(String type, String rate) {
        return MutingPipe.class.getResourceAsStream("/commercial-" + type + "-" + rate + ".raw");
    }

    private void safeLoop() {
        while (!stop) {
            if (index == 0 && System.currentTimeMillis() - lastUpdate > UNMUTE_AFTER) {
                LOG.info("Unmuting after 5 minutes");
                onJingle(1);
                analyzer.setNextJingleIndex(0);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOG.error("Interrupted", e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        stop = true;
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS))
                ;
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}
