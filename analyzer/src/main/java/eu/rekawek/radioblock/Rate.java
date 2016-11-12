package eu.rekawek.radioblock;

import javax.sound.sampled.AudioFormat;

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

    public static Rate getFromFormat(AudioFormat format) {
        int r = (int) format.getFrameRate();
        switch (r) {
            case 32000:
                if (format.getChannels() == 1) {
                    return RATE_32_MONO;
                }
                break;
            case 44100:
                if (format.getChannels() == 2) {
                    return RATE_44_1;
                }
                break;
            case 48000:
                if (format.getChannels() == 2) {
                    return RATE_48;
                }
                break;
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }
}