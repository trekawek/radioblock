#!/bin/bash

MUTE=10

ffmpeg -loglevel -8 -i mmsh://stream.polskieradio.pl/program3?MSWMExt=.asf -f s16le -acodec pcm_s16le - \
 | java -jar radioblock-1.0.0-SNAPSHOT.jar \
 | while read line
do
  if [ "$line" == "Got jingle 0" ]; then
    OLD_VOLUME=$(./yamaha.rb volume)
    ./yamaha.sh volume $MUTE > /dev/null
    echo "Setting volume to $MUTE"
  elif [ "$line" == "Got jingle 1" ]; then
    NEW_VOLUME=$(./yamaha.rb volume)
    if [ "$NEW_VOLUME" -eq "$MUTE" ]; then
      ./yamaha.sh volume $OLD_VOLUME > /dev/null
      echo "Restoring volume to $OLD_VOLUME"
    else
      echo "Volume has been updated manually to $NEW_VOLUME"
    fi
  fi
done
