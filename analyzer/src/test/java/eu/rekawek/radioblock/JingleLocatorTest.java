package eu.rekawek.radioblock;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.rekawek.radioblock.JingleLocator;
import eu.rekawek.radioblock.JingleLocator.JingleListener;

@RunWith(Parameterized.class)
public class JingleLocatorTest {

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { Rate.RATE_32_MONO }, { Rate.RATE_44_1 }, { Rate.RATE_48 } });
    }

    private final Rate rate;

    public JingleLocatorTest(Rate rate) {
        this.rate = rate;
    }

    @Test(timeout = 30000)
    public void locateTest() throws IOException, InterruptedException {
        List<InputStream> jingles = new ArrayList<InputStream>();
        for (String name : asList(rate.getSamples())) {
            File file = new File("src/main/resources", name);
            InputStream is = new FileInputStream(file);
            jingles.add(is);
        }
        JingleLocator locator = new JingleLocator(jingles, rate.getChannels(), 200);

        final List<Integer> foundJingles = new ArrayList<Integer>();
        locator.addListener(new JingleListener() {
            @Override
            public void gotJingle(int index, float level) {
                foundJingles.add(index);
            }
        });

        locator.analyse(new FileInputStream(getCommercialBlockFile()));
        assertEquals(Arrays.asList(0, 1), foundJingles);
    }

    private File getCommercialBlockFile() {
        File commercialBlock = null;
        switch (rate) {
        case RATE_32_MONO:
            commercialBlock = new File("src/test/resources/commercial-block-32k-mono.raw");
            break;
        case RATE_44_1:
            commercialBlock = new File("src/test/resources/commercial-block-44.1k.raw");
            break;
        case RATE_48:
            commercialBlock = new File("src/test/resources/commercial-block-48k.raw");
            break;
        default:
            Assert.fail("Illegal rate: " + rate);
            break;
        }
        return commercialBlock;
    }
}
