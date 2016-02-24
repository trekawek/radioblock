#!/bin/bash

ANALYZER_PID=0
RADIO=0

function start_analyzer {
  ./analyzer.sh &
  ANALYZER_PID="$!"
  echo "started analyzer with pid $ANALYZER_PID"
}

function stop_analyzer {
  pkill java -jar radioblock
  echo "stopped analyzer with pid $ANALYZER_PID"
  ANALYZER_PID="0"
}

while :
do
  RADIO=$(./yamaha.sh is_radio)
  if [ "$RADIO" -eq 1 ] && [ "$ANALYZER_PID" -eq 0 ]; then
    start_analyzer
  elif [ "$RADIO" -eq 0 ] && [ "$ANALYZER_PID" -ne 0 ]; then
    stop_analyzer
  fi
  sleep 1
done
