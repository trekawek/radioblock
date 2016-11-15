package eu.rekawek.radioblock;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import eu.rekawek.analyzer.AnalysisListener;
import eu.rekawek.analyzer.Analyzer;
import eu.rekawek.analyzer.AnalyzerBuilder;
import eu.rekawek.analyzer.channel.MultiplexingStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AnalyzerTest {

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "32k-mono", 1 }, { "44.1k", 2 }, { "48k", 2 } });
    }

    private final String suffix;

    private final int channels;

    public AnalyzerTest(String suffix, int channels) {
        this.suffix = suffix;
        this.channels = channels;
    }

    @Test(timeout = 30000)
    public void locateTest() throws IOException, InterruptedException {
        AnalyzerBuilder builder = new AnalyzerBuilder();
        builder.setChannels(channels);
        builder.setMultiplexingStrategy(MultiplexingStrategy.AVERAGE);
        builder.addJingle("0", getFile("commercial-start"), 200);
        builder.addJingle("1", getFile("commercial-end"), 200);
        Analyzer analyzer = builder.build();

        final List<String> foundJingles = new ArrayList<String>();
        analyzer.addListener(new AnalysisListener() {
            @Override
            public void analysisInProgress(String expectedId, int expectedJingleIndex, int[] levels) {
            }

            @Override
            public void gotJingle(String id, int jingleIndex, int[] levels) {
                foundJingles.add(id);
            }
        });
        analyzer.analyze(getFile("commercial-block"));
        assertEquals(Arrays.asList("0", "1"), foundJingles);
    }

    private InputStream getFile(String prefix) throws FileNotFoundException {
        return new FileInputStream(new File("src/test/resources/" + prefix + "-" + suffix + ".raw"));
    }
}
