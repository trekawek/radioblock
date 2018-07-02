package eu.rekawek.analyzer.sample;

import org.jtransforms.fft.FloatFFT_1D;

import java.util.Iterator;

import static com.google.common.collect.Iterators.transform;

public class Waveform {

    private final float[] buffer;

    private final int length;

    private Waveform(float[] buffer, int length) {
        this.buffer = buffer;
        this.length = length;
    }

    public static Waveform createFromSignedShort(Iterator<Short> waveform, int length) {
        return createFromSignedShort(waveform, 1, length)[0];
    }

    public static Waveform[] createFromSignedShort(Iterator<Short> waveform, int channels, int length) {
        Iterator<Float> floatIt = transform(waveform, s -> s / 32768f);
        float[][] buffers = demultiplex(floatIt, channels, length);
        Waveform[] waveforms = new Waveform[channels];
        for (int c = 0; c < channels; c++) {
            waveforms[c] = new Waveform(buffers[c], length);
        }
        return waveforms;
    }

    public static float[][] demultiplex(Iterator<Float> floatIt, int channels, int length) {
        float[][] buffers = new float[channels][length * 2];
        int i = 0;
        int c = 0;
        while (floatIt.hasNext()) {
            buffers[c++][i] = floatIt.next();
            if (c == channels) {
                c = 0;
                i++;
            }
        }
        return buffers;
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
