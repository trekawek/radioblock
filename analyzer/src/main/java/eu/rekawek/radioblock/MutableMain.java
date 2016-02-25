package eu.rekawek.radioblock;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MutableMain {

    private static final Logger LOG = LoggerFactory.getLogger(JingleLocator.class);

    public static void main(String... args) throws IOException, URISyntaxException {
        Rate rate = Rate.RATE_48;
        if (args.length == 1) {
            rate = Rate.valueOf(args[0]);
        }
        LOG.info("Using rate {}", rate);

        MutingPipe pipe = new MutingPipe(rate);
        pipe.copyStream(System.in, System.out);
    }

}
