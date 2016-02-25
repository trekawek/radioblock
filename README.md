# RadioBlock

AdBlock for the polish public radio [Trójka](http://www.polskieradio.pl/9,Trojka). Uses cross correlation to find ad jingles and mutes the whole ads block.

## Usage

    ffmpeg -loglevel -8 \
           -i mmsh://stream.polskieradio.pl/program3?MSWMExt=.asf \
           -f s16le -acodec pcm_s16le - \
    | java -cp analyzer/target/analyzer-1.0.0-SNAPSHOT.jar eu.rekawek.radioblock.MutableMain \
    | play -r 48000 -b 16 -c 2 -e signed -t raw -