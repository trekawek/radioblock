# RadioBlock

AdBlock for the polish public radio [Trójka](http://www.polskieradio.pl/9,Trojka). Uses cross correlation to find ad jingles and mutes the whole ads block. More info can be found in a blog post: [part 1](http://blog.rekawek.eu/2016/02/24/radio-adblock/), [part 2](http://blog.rekawek.eu/2016/02/27/radio-adblock-2/).

## Usage

### Case 1 - detecting ads

    ffmpeg -loglevel -8 \
           -i http://stream3.polskieradio.pl:8904/\;stream \
           -f s16le -acodec pcm_s16le - \
      | java -jar analyzer-1.0.0-SNAPSHOT.jar RATE_44_1 500 800

500 and 800 are the thresholds for detecting ad start/stop jingle. Possible rate values:

* RATE_32_MONO,
* RATE_44_1,
* RATE_48.

### Case 2 - GUI

    java -jar standalone-1.0.0-SNAPSHOT.jar

### Case 3 - write the muted stream to stdout

    java -jar standalone-1.0.0-SNAPSHOT.jar --cli [OPENING_THRESHOLD [CLOSING_THRESHOLD]]
    
Output format is PCM, 48000, stereo, low-endian.