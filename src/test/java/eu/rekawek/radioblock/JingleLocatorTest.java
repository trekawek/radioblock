package eu.rekawek.radioblock;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import eu.rekawek.radioblock.JingleLocator;
import eu.rekawek.radioblock.JingleLocator.JingleListener;

public class JingleLocatorTest {

    @Test(timeout = 30000)
    public void locateTest() throws IOException, InterruptedException {
        List<InputStream> jingles = new ArrayList<InputStream>();
        for (String name : asList("commercial-start.raw", "commercial-end.raw")) {
            File file = new File("src/main/resources", name);
            InputStream is = new FileInputStream(file);
            jingles.add(is);
        }
        JingleLocator locator = new JingleLocator(jingles);

        final List<Integer> foundJingles = new ArrayList<Integer>();
        locator.addListener(new JingleListener() {
            @Override
            public void gotJingle(int index) {
                foundJingles.add(index);
            }
        });
        locator.analyse(new FileInputStream(new File("src/test/resources/commercial-block-1.raw")));
        assertEquals(Arrays.asList(0, 1), foundJingles);
    }
}
