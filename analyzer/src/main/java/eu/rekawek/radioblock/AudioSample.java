package eu.rekawek.radioblock;

import static java.lang.Math.max;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Iterator;

import org.jtransforms.fft.FloatFFT_1D;

public class AudioSample {

    private final float[] buffer;

    private final int sampleLength;

    public AudioSample(int prePadding, int size, Iterable<Short> values, int postPadding) {
        sampleLength = max(size, max(prePadding, postPadding));
        buffer = new float[sampleLength * 2];
        int i = 0;
        if (prePadding > size) {
            i = prePadding - size;
        }
        for (short s : values) {
            buffer[i++] = s / 32768f;
        }
    }

    public static AudioSample fromBuffer(final int channels, int prePadding, byte[] buffer, int postPadding) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        final ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        AudioSample sample = new AudioSample(prePadding, shortBuffer.limit() / channels, new Iterable<Short>() {
            @Override
            public Iterator<Short> iterator() {
                return new PcmBufferIterator(channels, shortBuffer);
            }
        }, postPadding);
        return sample;
    }

    public AudioSample doFft() {
        FloatFFT_1D fft = new FloatFFT_1D(sampleLength);
        fft.realForwardFull(buffer);
        return this;
    }

    public void doConjAndMultiply(AudioSample sample) {
        int minLength = Math.min(buffer.length, sample.buffer.length);
        int i;
        for (i = 0; i < minLength; i += 2) {
            float a = buffer[i];
            float b = -buffer[i + 1];
            float c = sample.buffer[i];
            float d = sample.buffer[i + 1];

            buffer[i] = a * c - b * d;
            buffer[i + 1] = a * d + b * c;
        }
        for (; i < buffer.length; i++) {
            buffer[i] = 0;
        }
    }

    public void doIfft() {
        FloatFFT_1D fft = new FloatFFT_1D(sampleLength);
        fft.complexInverse(buffer, true);
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
