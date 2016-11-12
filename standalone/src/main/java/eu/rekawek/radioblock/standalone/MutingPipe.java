package eu.rekawek.radioblock.standalone;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.rekawek.radioblock.JingleLocator;
import eu.rekawek.radioblock.Rate;
import org.apache.commons.io.input.TeeInputStream;

import eu.rekawek.radioblock.JingleLocator.JingleListener;

public class MutingPipe {

    private final JingleLocator locator;

    private final List<JingleListener> listeners;

    public MutingPipe(Rate rate, int openingThreshold, int closingThreshold) throws IOException {
        List<InputStream> jingles = asList(rate.getSamples()).stream().map(MutingPipe.class.getClassLoader()::getResourceAsStream).collect(toList());
        locator = new JingleLocator(jingles, Arrays.asList(openingThreshold, closingThreshold), rate.getChannels());
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

    public void setThreshold(int jingleIndex, int newLevel) {
        locator.setThreshold(jingleIndex, newLevel);
    }
}
