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
        Rate rate = Rate.valueOf(args[0]);
        List<Integer> thresholds = new ArrayList<Integer>();
        thresholds.add(Integer.parseInt(args[1]));
        thresholds.add(Integer.parseInt(args[2]));
        LOG.info("Using rate {}, thresholds {}", rate, thresholds);

        List<InputStream> jingles = new ArrayList<InputStream>();
        for (String name : asList(rate.getSamples())) {
            jingles.add(Main.class.getClassLoader().getResourceAsStream(name));
        }
        JingleLocator locator = new JingleLocator(jingles, thresholds, rate.getChannels());
        locator.addListener(new JingleListener() {
            @Override
            public void gotJingle(int index, float level) {
             System.out.println("Got jingle " + index);
            }
        });
        locator.analyse(new FuseInputStream(System.in, 1000, new Runnable() {
            @Override
            public void run() {
                System.exit(0);
            }
        }));
    }
}