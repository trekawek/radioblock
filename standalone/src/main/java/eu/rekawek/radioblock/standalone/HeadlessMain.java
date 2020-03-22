package eu.rekawek.radioblock.standalone;

import com.jssrc.resample.JSSRCResampler;
import eu.rekawek.analyzer.AnalysisListener;
import eu.rekawek.radioblock.standalone.stream.RadioStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HeadlessMain {

    private static final Logger LOG = LoggerFactory.getLogger(HeadlessMain.class);

    private static final AudioFormat FORMAT_STEREO = new AudioFormat(48000, 16, 2, true, false);

    private static final AudioFormat FORMAT_MONO = new AudioFormat(48000, 16, 1, true, false);

    public static void main(String[] args) throws IOException {
        int openingThreshold = PlayerPrefs.OPENING_THRESHOLD_DEFAULT;
        int closingThreshold = PlayerPrefs.CLOSING_THRESHOLD_DEFAULT;
        if (args.length >= 1) {
            openingThreshold = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            closingThreshold = Integer.parseInt(args[1]);
        }

        InputStream stream;
        AudioFormat format;

        EventServer fifoServer;
        if (System.getProperty("fifo") == null) {
            fifoServer = null;
        } else {
            fifoServer = new EventServer(System.getProperty("fifo"));
        }

        if (Arrays.asList(args).contains("--stdin")) {
            stream = System.in;
            if (Arrays.asList(args).contains("--mono")) {
                format = FORMAT_MONO;
            } else {
                format = FORMAT_STEREO;
            }
        } else {
            RadioStream broadcast = RadioStreamProvider.getStream();
            if (broadcast.getAudioFormat().matches(FORMAT_STEREO)) {
                format = broadcast.getAudioFormat();
                stream = broadcast;
            } else {
                format = FORMAT_STEREO;
                stream = new JSSRCResampler(broadcast.getAudioFormat(), FORMAT_STEREO, broadcast);
            }
        }

        MutingPipe pipe = new MutingPipe(format, openingThreshold, closingThreshold);

        AnalyserFuse fuse = new AnalyserFuse(new int[] {openingThreshold, closingThreshold});
        pipe.addListener(fuse);
        if (fifoServer != null) {
            pipe.addListener((id, expectedJingleIndex, levels) -> fifoServer.update((short) IntStream.of(levels).sum(), (byte) ("opening".equals(id) ? 0 : 1)));
        }

        Thread fuseThread = new Thread(fuse);
        fuseThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down");
            fuseThread.interrupt();
            try {
                if (fifoServer != null) {
                    fifoServer.close();
                }
                pipe.close();
            } catch (IOException e) {
                LOG.error("Can't close pipe", e);
            }
        }));

        pipe.copyStream(stream, System.out);
    }

    private static class AnalyserFuse implements AnalysisListener, Runnable {

        private static final long TIMEOUT_MILLIS = TimeUnit.HOURS.toMillis(12);

        private volatile boolean alive;

        private int[] thresholds;

        public AnalyserFuse(int[] thresholds) {
            this.thresholds = thresholds;
        }

        @Override
        public void analysisInProgress(String expectedId, int expectedJingleIndex, int[] levels) {
            int l = IntStream.of(levels).sum();
            int t = thresholds[expectedJingleIndex];
            if (l > t / 2) {
                alive = true;
            }
        }

        @Override
        public void gotJingle(String id, int expectedJingleIndex, int[] levels) {
            alive = true;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    alive = false;
                    Thread.sleep(TIMEOUT_MILLIS);
                    if (!alive) {
                        LOG.info("No activity for {}ms", TIMEOUT_MILLIS);
                        System.exit(1);
                        break;
                    }
                } catch (InterruptedException e) {
                    LOG.info("Interrupted fuse loop");
                    break;
                }
            }
        }
    }
}