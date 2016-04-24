package eu.rekawek.radioblock;

public enum Rate {
    RATE_32_MONO(1, "commercial-start-32k-mono.raw", "commercial-end-32k-mono.raw"),
    RATE_44_1(2, "commercial-start-44.1k.raw", "commercial-end-44.1k.raw"),
    RATE_48(2, "commercial-start-48k.raw", "commercial-end-48k.raw");

    private final String[] samples;

    private final int channels;

    Rate(int channels, String... samples) {
        this.samples = samples;
        this.channels = channels;
    }

    public String[] getSamples() {
        return samples;
    }

    public int getChannels() {
        return channels;
    }
}