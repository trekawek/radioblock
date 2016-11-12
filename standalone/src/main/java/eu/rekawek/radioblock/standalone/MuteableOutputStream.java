package eu.rekawek.radioblock.standalone;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MuteableOutputStream extends OutputStream {

    private final ByteBuffer buffer;

    private final OutputStream os;

    private volatile float volumeLevel = 1;

    public MuteableOutputStream(OutputStream os) {
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