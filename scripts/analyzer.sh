#!/bin/bash

MUTE=10
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

rtl_fm -M wbfm -f 89.50M -g 0 \
 | java -jar analyzer-1.0.0-SNAPSHOT.jar 1 \
   $DIR/commercial-start-32k-mono.raw 180 \
   $DIR/commercial-end-32k-mono.raw 190 \
 | while read line
do
  if [ "$line" == "Got jingle 0" ]; then
    OLD_VOLUME=$(./yamaha.sh volume)
    ./yamaha.sh volume $MUTE > /dev/null
    echo "$(date) Setting volume to $MUTE"
  elif [ "$line" == "Got jingle 1" ]; then
    NEW_VOLUME=$(./yamaha.sh volume)
    if [ "$NEW_VOLUME" -eq "$MUTE" ]; then
      ./yamaha.sh volume $OLD_VOLUME > /dev/null
      echo "$(date) Restoring volume to $OLD_VOLUME"
    else
      echo "$(date) Volume has been updated manually to $NEW_VOLUME"
    fi
  fi
done
