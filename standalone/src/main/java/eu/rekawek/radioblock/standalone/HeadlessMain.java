package eu.rekawek.radioblock.standalone;

import com.jssrc.resample.JSSRCResampler;
import eu.rekawek.analyzer.AnalysisListener;
import eu.rekawek.radioblock.standalone.stream.RadioStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HeadlessMain {

    private static final Logger LOG = LoggerFactory.getLogger(HeadlessMain.class);

    private static final AudioFormat FORMAT = new AudioFormat(48000, 16, 2, true, false);

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        int openingThreshold = PlayerPrefs.OPENING_THRESHOLD_DEFAULT;
        int closingThreshold = PlayerPrefs.CLOSING_THRESHOLD_DEFAULT;
        if (args.length >= 1) {
            openingThreshold = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            closingThreshold = Integer.parseInt(args[1]);
        }

        RadioStream broadcast = RadioStreamProvider.getStream();

        AudioFormat format;
        InputStream stream;

        if (broadcast.getAudioFormat().matches(FORMAT)) {
            format = broadcast.getAudioFormat();
            stream = broadcast;
        } else {
            format = FORMAT;
            stream = new JSSRCResampler(broadcast.getAudioFormat(), FORMAT, broadcast);
        }

        MutingPipe pipe = new MutingPipe(format, openingThreshold, closingThreshold);
        AnalyserFuse fuse = new AnalyserFuse(new int[] {openingThreshold, closingThreshold});
        pipe.addListener(fuse);
        new Thread(fuse).start();
        pipe.copyStream(stream, System.out);
    }

    private static class AnalyserFuse implements AnalysisListener, Runnable {

        private static final long TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(10);

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
                    LOG.error("Interrupted fuse loop", e);
                    break;
                }
            }
        }
    }
}