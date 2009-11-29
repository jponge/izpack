#!/bin/sh
/usr/local/bin/xvfb-run -s "-screen 0 1280x1024x24" mvn clean install
