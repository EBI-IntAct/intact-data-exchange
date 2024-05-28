#!/bin/bash

MAVEN_OPTS="-Xms512m -Xmx4048m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

MAVEN_PROFILE=$5

mvn clean install -Pbinary-export,${MAVEN_PROFILE} -Drule=$1 -Dmitab=$2 -DfileExported=$3 -DfileExcluded=$4 -Dmaven.test.skip -Ddb=postgres