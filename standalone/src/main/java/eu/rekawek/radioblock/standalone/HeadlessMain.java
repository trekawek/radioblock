package eu.rekawek.radioblock.standalone;

import com.jssrc.resample.JSSRCResampler;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

public class HeadlessMain {

    private static final AudioFormat FORMAT = new AudioFormat(48000, 16, 2, true, false);

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        int openingThreshold = PlayerPrefs.OPENING_THRESHOLD_DEFAULT;
        int closingThreshold = PlayerPrefs.CLOSING_THRESHOLD_DEFAULT;
        if (args.length >= 1) {
            openingThreshold = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            closingThreshold = Integer.parseInt(args[1]);
        }

        RadioStreamProvider.RadioStream broadcast = RadioStreamProvider.getStream();

        AudioFormat format;
        InputStream stream;

        if (broadcast.getFormat().matches(FORMAT)) {
            format = broadcast.getFormat();
            stream = broadcast.getStream();
        } else {
            format = FORMAT;
            stream = new JSSRCResampler(broadcast.getFormat(), FORMAT, broadcast.getStream());
        }

        MutingPipe pipe = new MutingPipe(format, openingThreshold, closingThreshold);
        pipe.copyStream(stream, System.out);
    }
}