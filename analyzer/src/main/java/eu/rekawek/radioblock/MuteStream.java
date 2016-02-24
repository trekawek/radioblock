package eu.rekawek.radioblock;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.TeeInputStream;

import eu.rekawek.radioblock.JingleLocator.JingleListener;

public class MuteStream {

    public static void main(String... args) throws IOException, URISyntaxException {
        List<InputStream> jingles = new ArrayList<InputStream>();
        for (String name : asList("commercial-start.raw", "commercial-end.raw")) {
            jingles.add(MuteStream.class.getClassLoader().getResourceAsStream(name));
        }
        JingleLocator locator = new JingleLocator(jingles, 200);
        final MutableOutputStream mos = new MutableOutputStream(System.out);
        locator.addListener(new JingleListener() {
            @Override
            public void gotJingle(int index, float level) {
                if (index == 0) {
                    mos.setVolumeLevel(0.1f);
                } else {
                    mos.setVolumeLevel(1);
                }
            }
        });
        TeeInputStream tis = new TeeInputStream(System.in, mos);
        locator.analyse(tis);
    }

    public static class MutableOutputStream extends OutputStream {

        private final ByteBuffer buffer;

        private final OutputStream os;

        private volatile float volumeLevel = 1;

        public MutableOutputStream(OutputStream os) {
            this.buffer = ByteBuffer.allocate(4);
            this.buffer.order(ByteOrder.LITTLE_ENDIAN);
            this.os = os;
        }

        @Override
        public void write(int b) throws IOException {
            buffer.put((byte) b);
            if (buffer.position() == 4) {
                float volumeLevelLocal = volumeLevel;

                buffer.rewind();
                short left = (short) (buffer.getShort() * volumeLevelLocal);
                short right = (short) (buffer.getShort() * volumeLevelLocal);

                buffer.clear();
                buffer.putShort(left);
                buffer.putShort(right);
                buffer.rewind();
                while (buffer.hasRemaining()) {
                    os.write(buffer.get());
                }
                buffer.clear();
            }
        }

        public void setVolumeLevel(float volumeLevel) {
            this.volumeLevel = volumeLevel;
        }
    }
}
