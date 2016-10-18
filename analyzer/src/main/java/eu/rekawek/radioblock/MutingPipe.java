package eu.rekawek.radioblock;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.input.TeeInputStream;

import eu.rekawek.radioblock.JingleLocator.JingleListener;

public class MutingPipe {

    private final JingleLocator locator;

    private final List<JingleListener> listeners;

    public MutingPipe(Rate rate) throws IOException {
        List<InputStream> jingles = new ArrayList<InputStream>();
        for (String name : asList(rate.getSamples())) {
            jingles.add(Main.class.getClassLoader().getResourceAsStream(name));
        }
        locator = new JingleLocator(jingles, Arrays.asList(500, 550), rate.getChannels());
        listeners = new ArrayList<>();
    }

    public void addListener(JingleListener listener) {
        listeners.add(listener);
    }

    public void copyStream(InputStream is, OutputStream os) {
        final MuteableOutputStream mos = new MuteableOutputStream(os);
        locator.addListener((index, level) -> {
            if (index == 0) {
                mos.setVolumeLevel(0.01f);
            } else {
                mos.setVolumeLevel(1);
            }
        });
        listeners.forEach(locator::addListener);
        TeeInputStream tis = new TeeInputStream(is, mos);
        locator.analyse(tis);
    }
}
