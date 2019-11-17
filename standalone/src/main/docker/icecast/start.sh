#!/bin/sh -e

mv icecast.xml icecast.xml.tmpl
envsubst < icecast.xml.tmpl > icecast.xml

exec icecast -c icecast.xml