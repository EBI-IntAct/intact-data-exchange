#!/bin/bash

MAVEN_OPTS="-Xms512m -Xmx4048m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

mvn clean install -Pexport-filter -DfileA=$1 -DfileB=$2 -Dresults=$3 -Dmaven.test.skip