package eu.rekawek.radioblock.standalone;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

public class IceStreamReader implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(IceStreamReader.class);

    private final URL url;

    private final int bitrate;

    private final OutputStream os;

    private volatile boolean stop;

    public IceStreamReader(URL url, int bitrate, OutputStream os) {
        this.url = url;
        this.bitrate = bitrate;
        this.os = os;
    }

    @Override
    public void run() {
        try {
            read();
        } catch (Exception e) {
            LOG.error("Can't read stream", e);
        }
    }

    private void read() throws BitstreamException, IOException, DecoderException {
        Socket socket = new Socket(url.getHost(), url.getPort());
        sendGetRequest(socket);

        InputStream is = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while (!"".equals(reader.readLine()))
            ;

        BufferedInputStream bis = new BufferedInputStream(is);
        Bitstream bitstream = new Bitstream(bis);
        Decoder decoder = new Decoder();
        SampleBuffer buffer = new SampleBuffer(bitrate, 2);
        ByteBuffer byteBuffer = ByteBuffer.allocate(buffer.getBuffer().length * 2);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        decoder.setOutputBuffer(buffer);

        while (!stop) {
            Header frame = bitstream.readFrame();
            decoder.decodeFrame(frame, bitstream);
            bitstream.closeFrame();
            byteBuffer.clear();
            for (int i = 0; i < buffer.getBufferLength(); i++) {
                byteBuffer.putShort(buffer.getBuffer()[i]);
            }
            os.write(byteBuffer.array(), 0, byteBuffer.position());
            buffer.clear_buffer();
        }
        socket.close();
    }

    public void stop() {
        stop = true;
    }

    private void sendGetRequest(Socket socket) throws IOException {
        OutputStream socketOs = socket.getOutputStream();
        socketOs.write(("GET " + url.getPath() + " HTTP/1.0\r\n\r\n").getBytes());
    }

}
