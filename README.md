# RadioBlock

AdBlock for the polish public radio [Trójka](http://www.polskieradio.pl/9,Trojka). Uses cross correlation to find ad jingles and mutes the whole ads block. More info can be found in a blog post: [part 1](http://blog.rekawek.eu/2016/02/24/radio-adblock/), [part 2](http://blog.rekawek.eu/2016/02/27/radio-adblock-2/).

## Usage

### Case 1 - detecting ads

The analyzer modules is a CLI application reading the stream from the standard input and trying to find the configured samples. For instance, the command below uses ffmpeg to generate the raw stream of Polish "Trójka" and looks for the commercial start and stop jingles, with thresholds 500 and 700, respectively. The "2" describes the channel count:

```bash
cd analyzer
ffmpeg -loglevel -8 \
       -i http://stream3.polskieradio.pl:8904/\;stream \
       -f s16le -acodec pcm_s16le - \
  | java -jar target/analyzer-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
    2 \
    src/test/resources/commercial-start-44.1k.raw 500 \
    src/test/resources/commercial-end-44.1k.raw 700
```

Another example - reading the audio stream from the RTL-SDR device:

```bash
rtl_fm -M wbfm -f 89.50M -g 0 \
 | java -jar analyzer-*.jar 1 \
   commercial-start-32k-mono.raw 180 \
   commercial-end-32k-mono.raw 190 \
```

### Case 2 - GUI

A standalone "Trójka" player with GUI:

```bash
java -jar standalone/target/standalone-1.0.0-SNAPSHOT.jar
```

### Case 3 - write the muted stream to stdout

A command line "Trójka" player, passing the stream to stdout. Output format is PCM, 48000, stereo, low-endian. Example:

```bash
java -jar standalone/target/standalone-1.0.0-SNAPSHOT.jar --cli 500 700 \
  | play -t raw -b 16 -c 2 -r 48000 -e signed-integer -
```
