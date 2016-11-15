package eu.rekawek.radioblock.standalone;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import eu.rekawek.analyzer.AnalysisListener;
import eu.rekawek.analyzer.Analyzer;
import eu.rekawek.analyzer.AnalyzerBuilder;
import eu.rekawek.analyzer.channel.MultiplexingStrategy;
import org.apache.commons.io.input.TeeInputStream;

import javax.sound.sampled.AudioFormat;

public class MutingPipe {

    private final Analyzer analyzer;

    private MuteableOutputStream mos;

    public MutingPipe(AudioFormat format, int openingThreshold, int closingThreshold) throws IOException {
        String rate = getRate(format);

        AnalyzerBuilder builder = new AnalyzerBuilder();
        builder.setChannels(2);
        builder.setMultiplexingStrategy(MultiplexingStrategy.AVERAGE);
        builder.addJingle("opening", getJingleStream("start", rate), openingThreshold);
        builder.addJingle("closing", getJingleStream("end", rate), closingThreshold);
        analyzer = builder.build();
        analyzer.addListener((id, jingleIndex, levels) -> {
            if (mos == null) {
                return;
            }
            if (jingleIndex == 0) {
                mos.setVolumeLevel(0.01f);
            } else {
                mos.setVolumeLevel(1);
            }
        });
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
}
