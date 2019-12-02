#!/bin/sh -e

mv ices.xml ices.xml.tmpl
envsubst < ices.xml.tmpl > ices.xml

echo "Waiting for Icecast to initialize"
sleep 10

function watch_ices {
  while true; do
    sleep 5
    if ! pgrep ices; then
      pkill java
      exit
    fi
  done
}

watch_ices &

java -jar standalone.jar --cli 600 700 | exec ices ices.xml
