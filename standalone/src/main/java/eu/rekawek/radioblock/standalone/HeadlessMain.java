package eu.rekawek.radioblock.standalone;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class HeadlessMain {

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        int openingThreshold = PlayerPrefs.OPENING_THRESHOLD_DEFAULT;
        int closingThreshold = PlayerPrefs.CLOSING_THRESHOLD_DEFAULT;
        if (args.length == 2) {
            openingThreshold = Integer.parseInt(args[0]);
            closingThreshold = Integer.parseInt(args[1]);
        }

        RadioStreamProvider.RadioStream stream = RadioStreamProvider.getStream();
        if (Math.abs(stream.getFormat().getFrameRate() - 48000.0) > 1) {
            System.err.println("Can't open the w.dktr.pl stream");
            System.exit(1);
            return;
        }

        MutingPipe pipe = new MutingPipe(stream.getFormat(), openingThreshold, closingThreshold);
        pipe.copyStream(stream.getStream(), System.out);
    }

}
