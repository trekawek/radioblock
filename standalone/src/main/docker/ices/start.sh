#!/bin/sh -e

mv ices.xml ices.xml.tmpl
envsubst < ices.xml.tmpl > ices.xml

echo "Waiting for Icecast to initialize"
sleep 10

java -jar standalone.jar --cli 600 700 | exec ices ices.xml