package eu.rekawek.radioblock;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import eu.rekawek.radioblock.JingleLocator.JingleListener;

public class Main {

    public static void main(String... args) throws IOException, URISyntaxException {
        List<InputStream> jingles = new ArrayList<InputStream>();
        for (String name : asList("commercial-start.raw", "commercial-end.raw")) {
            jingles.add(Main.class.getClassLoader().getResourceAsStream(name));
        }
        JingleLocator locator = new JingleLocator(jingles, 200);
        locator.addListener(new JingleListener() {
            @Override
            public void gotJingle(int index, float level) {
                System.out.println("Got jingle " + index);
            }
        });
        locator.analyse(System.in);
    }

}
