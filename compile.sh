#!/bin/sh

mvn clean install -DskipTests jib:dockerBuild -P ci -s ./settings.xml
