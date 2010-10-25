#!/bin/sh
export DISPLAY=:66
mvn clean install -Prun-xvfb -Pwith-gui-tests
