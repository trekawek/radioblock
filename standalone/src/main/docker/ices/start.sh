#!/bin/sh -e

mv ices.xml ices.xml.tmpl
envsubst < ices.xml.tmpl > ices.xml

exec java -jar standalone.jar --cli 600 700 | ices ices.xml