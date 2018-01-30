package eu.rekawek.analyzer.cli;

import eu.rekawek.analyzer.Analyzer;
import eu.rekawek.analyzer.AnalyzerBuilder;
import eu.rekawek.analyzer.channel.MultiplexingStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String... args) throws IOException {
        if (args.length < 3) {
            showUsage();
            return;
        }
        int channels = safeParseInt(args[0]);
        AnalyzerBuilder builder = new AnalyzerBuilder();
        builder.setChannels(channels);
        builder.setMultiplexingStrategy(MultiplexingStrategy.LEFT);
        for (int i = 1; i < args.length; i+=2) {
            String filename = args[i];
            int threshold = safeParseInt(args[i+1]);

            File f = new File(filename);
            builder.addJingle(f.getName(), new FileInputStream(f), threshold);
        }
        Analyzer analyzer = builder.build();

        analyzer.addListener((id, jingleIndex, levels) -> System.out.println("Got jingle " + jingleIndex));
        analyzer.analyze(new FuseInputStream(System.in, TimeUnit.MINUTES.toMillis(1), () -> System.exit(0)));
    }

    private static int safeParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            showUsage();
            System.exit(1);
            return 0;
        }
    }

    private static void showUsage() {
        System.out.println("java -jar analyzer.jar CHANNELS FILE1 THRESHOLD1 [FILE2 THRESHOLD2...]");
    }
}