#!/bin/sh
./xvfb-run -s "-screen 0 1280x1024x24" $MAVEN_HOME/bin/mvn clean install
