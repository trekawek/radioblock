#!/storage/bin/bash-static

ANALYZER_PID=0
RADIO_TTL=0

cd /storage/.radioblock

start_analyzer() {
  ./analyzer.sh &
  ANALYZER_PID="$!"
  echo "$(date) started analyzer with pid $ANALYZER_PID"
}

stop_analyzer() {
  pkill -f 'analyzer-1.0.0-SNAPSHOT.jar'
  echo "$(date) stopped analyzer with pid $ANALYZER_PID"
  ANALYZER_PID="0"
}

while :
do
  if [ "$(./yamaha.sh is_radio)" -eq "1" ]; then
    if [ "$RADIO_TTL" -ne 3 ]; then
      echo "$(date) radio is enabled"
    fi
    RADIO_TTL=3
  elif [ "$RADIO_TTL" -gt 0  ]; then
    RADIO_TTL="$((RADIO_TTL-1))"
    echo "$(date) radio is disabled"
    cat last_command.log
    echo
  fi

  if [ "$RADIO_TTL" -ne 0 ] && [ "$ANALYZER_PID" -eq 0 ]; then
    start_analyzer
  elif [ "$RADIO_TTL" -eq 0 ] && [ "$ANALYZER_PID" -ne 0 ]; then
    stop_analyzer
  fi
  sleep 1
done
