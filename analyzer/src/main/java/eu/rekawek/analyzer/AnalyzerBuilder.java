package eu.rekawek.analyzer;

import eu.rekawek.analyzer.channel.MultiplexingStrategy;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AnalyzerBuilder {

    private int channels = 2;

    private MultiplexingStrategy multiplexingStrategy = MultiplexingStrategy.LEFT;

    private List<Jingle> jingleList = new ArrayList<>();

    public AnalyzerBuilder setChannels(int channels) {
        this.channels = channels;
        return this;
    }

    public AnalyzerBuilder setMultiplexingStrategy(MultiplexingStrategy multiplexingStrategy) {
        this.multiplexingStrategy = multiplexingStrategy;
        return this;
    }

    public AnalyzerBuilder addJingle(String id, InputStream stream, int threshold) throws IOException {
        jingleList.add(new Jingle(id, stream, threshold));
        return this;
    }

    public Analyzer build() {
        return new Analyzer(this);
    }

    int getChannels() {
        return channels;
    }

    MultiplexingStrategy getMultiplexingStrategy() {
        return multiplexingStrategy;
    }

    List<Jingle> getJingleList() {
        return jingleList;
    }

    public static class Jingle {

        private final String id;

        private final byte[] buffer;

        private int threshold;

        public Jingle(String id, InputStream inputStream, int threshold) throws IOException {
            this.id = id;
            this.buffer = IOUtils.toByteArray(inputStream);
            this.threshold = threshold;
        }

        public String getId() {
            return id;
        }

        public byte[] getBuffer() {
            return buffer;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }
    }
}
