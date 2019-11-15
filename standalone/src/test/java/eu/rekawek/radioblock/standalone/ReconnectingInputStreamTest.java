package eu.rekawek.radioblock.standalone;

import eu.rekawek.radioblock.standalone.stream.RadioStream;
import eu.rekawek.radioblock.standalone.stream.ReconnectingInputStream;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class ReconnectingInputStreamTest {

    @Test
    public void test() throws IOException {
        InputStream is = new ReconnectingInputStream(() -> new FailingInputStream(7));
        assertEquals(0, is.read());
        assertEquals(1, is.read());
        assertEquals(2, is.read());
        assertEquals(3, is.read());
        assertEquals(4, is.read());
        assertEquals(5, is.read());
        assertEquals(6, is.read());
        assertEquals(0, is.read());
        assertEquals(0, is.read());
        assertEquals(1, is.read());
        assertEquals(2, is.read());
    }

    private static class FailingInputStream extends RadioStream {

        private final int failAfter;

        private int index;

        public FailingInputStream(int failAfter) {
            this.failAfter = failAfter;
        }

        @Override
        public int read() throws IOException {
            if (failAfter == index) {
                throw new IOException();
            } else {
                return index++;
            }
        }

        @Override
        public AudioFormat getAudioFormat() {
            return new AudioFormat(44100, 16, 2, true, false);
        }
    }
}
