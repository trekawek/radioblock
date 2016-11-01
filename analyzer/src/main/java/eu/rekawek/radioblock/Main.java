package eu.rekawek.radioblock;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(JingleLocator.class);

    public static void main(String... args) throws IOException {
        Rate rate = Rate.valueOf(args[0]);
        List<Integer> thresholds = new ArrayList<Integer>();
        thresholds.add(Integer.parseInt(args[1]));
        thresholds.add(Integer.parseInt(args[2]));
        LOG.info("Using rate {}, thresholds {}", rate, thresholds);

        List<InputStream> jingles = asList(rate.getSamples()).stream().map(Main.class.getClassLoader()::getResourceAsStream).collect(toList());

        JingleLocator locator = new JingleLocator(jingles, thresholds, rate.getChannels());
        locator.addListener((index, level) -> System.out.println("Got jingle " + index));
        locator.analyse(new FuseInputStream(System.in, 1000, () -> System.exit(0)));
    }
}