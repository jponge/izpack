#!/bin/sh
/usr/local/bin/xvfb-run -s "-screen 0 1280x1024x24" /opt/maven/apache-maven-2.2.0/bin/mvn clean install
