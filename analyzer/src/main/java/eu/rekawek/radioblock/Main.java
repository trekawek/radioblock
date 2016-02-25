package eu.rekawek.radioblock;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rekawek.radioblock.JingleLocator.JingleListener;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(JingleLocator.class);

    public static void main(String... args) throws IOException, URISyntaxException {
        Rate rate = Rate.RATE_48;
        if (args.length == 1) {
            rate = Rate.valueOf(args[0]);
        }
        LOG.info("Using rate {}", rate);

        List<InputStream> jingles = new ArrayList<InputStream>();
        for (String name : asList(rate.getSamples())) {
            jingles.add(Main.class.getClassLoader().getResourceAsStream(name));
        }
        JingleLocator locator = new JingleLocator(jingles, rate.getChannels(), 200);
        locator.addListener(new JingleListener() {
            @Override
            public void gotJingle(int index, float level) {
                System.out.println("Got jingle " + index);
            }
        });
        locator.analyse(System.in);
    }
}