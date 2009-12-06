#!/bin/sh
MAVEN_BIN=/opt/maven/apache-maven-2.2.0/bin
XVFB_BIN=/usr/local/bin
#/usr/local/bin/xvfb-run -s "-screen 0 1280x1024x24" /opt/maven/apache-maven-2.2.0/bin/mvn clean install
$XVFB_BIN/xvfb-run -s "-screen 0 1280x1024x24" $MAVEN_BIN/mvn clean install
#XVFB_DISPLAY=:66
#XAUTH_FILE=/tmp/xauth-izpack
#killall Xvfb
#rm $XAUTH_FILE
#touch /tmp/xauth-izpack
#Xvfb $XVFB_DISPLAY -auth $XAUTH_FILE &
#sleep 5
#PID=pgrep Xvfb
#DISPLAY=$XVFB_DISPLAY
#/opt/maven/apache-maven-2.2.0/bin/mvn clean install
#kill $PID
#rm $XAUTH_FILE
