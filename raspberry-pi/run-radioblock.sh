#!/bin/bash -e

APP_HOME=/home/pi

mkdir -p "${APP_HOME}/fifo"
for f in lvl.fifo radioblock.fifo sa.fifo; do
  if [[ ! -e "${APP_HOME}/${f}" ]]; then
    mkfifo "${APP_HOME}/${f}"
  fi
done

#TYPE=levels
TYPE=spectrum_horizontal
#TYPE=spectrum_vertical

JINGLE1_LEVEL=250
JINGLE2_LEVEL=300

amixer -c 1 sset Speaker 37
amixer -c 1 sset Mic 12

sudo "${APP_HOME}/vu-meter-server.py" $TYPE &
vu_meter="$?"
trap "sudo kill $vu_meter" EXIT

arecord -D usb -c 1 -r 48000 -f S16_LE -t raw - \
 | java -Dfifo="${APP_HOME}/fifo/radioblock.fifo" -jar "${APP_HOME}"/standalone-*.jar --cli "${JINGLE1_LEVEL}" "${JINGLE2_LEVEL}" --stdin --mono \
 | sox -t raw -r 48000 -e signed -b 16 -c 1 - -t raw -r 48000 -e signed -b 16 -c 2 - \
 | aplay -c 2 -r 48000 -f S16_LE -t raw -
