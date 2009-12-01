#!/bin/sh
#/usr/local/bin/xvfb-run -s "-screen 0 1280x1024x24" /opt/maven/apache-maven-2.2.0/bin/mvn clean install
XVFB_DISPLAY=:90
XAUTH_FILE=/tmp/xauth-izpack
touch /tmp/xauth-izpack
Xvfb $XVFB_DISPLAY -auth $XAUTH_FILE &
DISPLAY=:90
mvn clean install
kill Xvfb
