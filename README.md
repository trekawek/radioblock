# RadioBlock

AdBlock for the polish public radio [Trójka](http://www.polskieradio.pl/9,Trojka). Uses cross correlation to find ad jingles and mutes the whole ads block. More info can be found in a [blog post](http://blog.rekawek.eu/2016/02/24/radio-adblock/).

## Usage

### Case 1 - detecting ads

ffmpeg -loglevel -8 \
       -i http://stream3.polskieradio.pl:8904/\;stream \
       -f s16le -acodec pcm_s16le - \
  | java -cp analyzer-1.0.0-SNAPSHOT.jar eu.rekawek.radioblock.Main RATE_44_1 \

### Case 2 - muting ads

ffmpeg -loglevel -8 \
       -i http://stream3.polskieradio.pl:8904/\;stream \
       -f s16le -acodec pcm_s16le - \
  | java -cp analyzer-1.0.0-SNAPSHOT.jar eu.rekawek.radioblock.MuteableMain RATE_44_1 \
  | play -r 44100 -b 16 -c 2 -e signed -t raw -


### Case 3 - gui

java -jar standalone-1.0.0-SNAPSHOT.jar
