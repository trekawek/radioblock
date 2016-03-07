#!/storage/bin/bash-static

MUTE=10

/storage/bin/rtl_fm -f 89.5M -M wbfm \
 | /storage/java/bin/java -jar /storage/.radioblock/analyzer-1.0.0-SNAPSHOT.jar RATE_32_MONO 80 120  \
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
