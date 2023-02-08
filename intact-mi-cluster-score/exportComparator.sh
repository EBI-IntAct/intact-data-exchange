#!/bin/bash

MAVEN_OPTS="-Xms512m -Xmx4048m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

mvn clean install -Pexport-comparator -DfileA=$1 -DfileB=$2 -Dfile1=$3 -Dfile2=$4 -Dfile3=$5 -Dmaven.test.skip