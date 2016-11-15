package eu.rekawek.analyzer.sample;

import org.jtransforms.fft.FloatFFT_1D;

import java.util.Iterator;

import static com.google.common.collect.Iterators.transform;

public class Waveform {

    private final float[] buffer;

    private final int length;

    public Waveform(Iterator<Float> waveform, int length) {
        this.buffer = new float[length * 2];
        this.length = length;

        int i = 0;
        while (waveform.hasNext()) {
            buffer[i++] = waveform.next();
        }
    }

    public static Waveform createFromSignedShort(Iterator<Short> waveform, int length) {
        return new Waveform(transform(waveform, s -> s / 32768f), length);
    }

    public Waveform doFft() {
        FloatFFT_1D fft = new FloatFFT_1D(length);
        fft.realForwardFull(buffer);
        return this;
    }

    public Waveform doConjAndMultiply(Waveform other) {
        int minLength = Math.min(buffer.length, other.buffer.length);
        int i;
        for (i = 0; i < minLength; i += 2) {
            float a = buffer[i];
            float b = -buffer[i + 1];
            float c = other.buffer[i];
            float d = other.buffer[i + 1];

            buffer[i] = a * c - b * d;
            buffer[i + 1] = a * d + b * c;
        }
        for (; i < buffer.length; i++) {
            buffer[i] = 0;
        }
        return this;
    }

    public Waveform doIfft() {
        FloatFFT_1D fft = new FloatFFT_1D(length);
        fft.complexInverse(buffer, true);
        return this;
    }

    public float getMaxReal(int range) {
        float max = Float.MIN_VALUE;
        for (int i = 0; i < range; i++) {
            if (max < buffer[i * 2]) {
                max = buffer[i * 2];
            }
        }
        return max;
    }
}
