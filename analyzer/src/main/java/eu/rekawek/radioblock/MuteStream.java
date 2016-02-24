package eu.rekawek.radioblock;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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
}
