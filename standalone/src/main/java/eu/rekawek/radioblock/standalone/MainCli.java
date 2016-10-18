package eu.rekawek.radioblock.standalone;

import eu.rekawek.radioblock.MutingPipe;
import eu.rekawek.radioblock.Rate;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class MainCli {

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        AudioInputStream ais = RadioStreamProvider.getStream();
        MutingPipe pipe = new MutingPipe(Rate.RATE_48);
        pipe.copyStream(ais, System.out);
    }
}
