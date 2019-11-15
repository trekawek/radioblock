package eu.rekawek.radioblock.standalone;

import eu.rekawek.radioblock.standalone.stream.IcecastInputStream;
import eu.rekawek.radioblock.standalone.stream.RadioStream;
import eu.rekawek.radioblock.standalone.stream.ReconnectingInputStream;
import eu.rekawek.radioblock.standalone.stream.VorbisInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RadioStreamProvider {

    private static final Logger LOG = LoggerFactory.getLogger(RadioStreamProvider.class);

    private static final String[] VORBIS_URL = new String[] {
            "http://41.dktr.pl:8000/trojka.ogg",
            "http://d.dktr.pl:8000/trojka.ogg",
    };

    private static final String[] ICECAST_URLS = new String[] {
            "http://stream3.polskieradio.pl:8904/;stream"
    };

    public static RadioStream getStream() {
        for (String url : VORBIS_URL) {
            try {
                return new ReconnectingInputStream(() -> new VorbisInputStream(url));
            } catch (IOException e) {
                LOG.error("Can't get stream {}", url, e);
            }
        }
        for (String url : ICECAST_URLS) {
            try {
                return new ReconnectingInputStream(() -> new IcecastInputStream(url));
            } catch (IOException e) {
                LOG.error("Can't get stream {}", url, e);
            }
        }
        return null;
    }
}
