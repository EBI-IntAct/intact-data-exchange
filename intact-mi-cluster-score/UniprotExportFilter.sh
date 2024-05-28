#!/bin/bash

MAVEN_OPTS="-Xms512m -Xmx4048m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

MAVEN_PROFILE=$3

mvn clean install -Puniprot-export-filter,${MAVEN_PROFILE} -Dsource=$1 -Dresults=$2 -DbinaryOnly=$4 -DhighConfidence=$5 -DproteinOnly=$6 -DpositiveOnly=$7 -Dmaven.test.skip -Ddb=postgres