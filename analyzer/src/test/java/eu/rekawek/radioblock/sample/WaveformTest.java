package eu.rekawek.radioblock.sample;

import eu.rekawek.analyzer.sample.Waveform;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class WaveformTest {

    @Test
    public void testDemultiplex() {
        Iterator<Float> it = Arrays.asList(1f, 1.5f, 2f, 2.5f, 3f, 3.5f).iterator();
        float[][] result = Waveform.demultiplex(it, 2, 3);

        assertEquals(2, result.length);

        // channel 0
        assertArrayEquals(new float[] {1f, 2f, 3f, 0f, 0f, 0f}, result[0], 0f);

        // channel 1
        assertArrayEquals(new float[] {1.5f, 2.5f, 3.5f, 0f, 0f, 0f}, result[1], 0f);
    }


    @Test
    public void testDemultiplexWithMissingElements() {
        Iterator<Float> it = Arrays.asList(1f, 1.5f, 2f, 2.5f, 3f).iterator();
        float[][] result = Waveform.demultiplex(it, 2, 3);

        assertEquals(2, result.length);

        // channel 0
        assertArrayEquals(new float[] {1f, 2f, 3f, 0f, 0f, 0f}, result[0], 0f);

        // channel 1
        assertArrayEquals(new float[] {1.5f, 2.5f, 0f, 0f, 0f, 0f}, result[1], 0f);
    }
}
