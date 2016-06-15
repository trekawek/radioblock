package eu.rekawek.radioblock.standalone;

import eu.rekawek.radioblock.MutingPipe;
import eu.rekawek.radioblock.Rate;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;

public class MainCli {

    public static void main(String[] args) throws IOException {
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);

        IceStreamReader reader = new IceStreamReader(new URL(Player.URL), 96000, pos);
        new Thread(reader).start();

        MutingPipe pipe = new MutingPipe(Rate.RATE_44_1);
        pipe.copyStream(pis, System.out);
    }
}
