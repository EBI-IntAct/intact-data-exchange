#!/bin/bash

# Runs the complex export
#
# Usage $0 folder
#
#
##################################################################

if [ $# -ne 2 ]; then
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: $0 DATABASE FILE_NAME"
      echo ""
      exit 1
fi


DATABASE=$1
FILE=$2

rm -rf target
mkdir target

mvn clean install -Pgpa-export,${DATABASE} -DoutputFile=${FILE}.gpa -Ddb=postgres -Dmaven.test.skip=true
mvn clean install -Pgpi-export,${DATABASE} -DoutputFile=${FILE}.gpi -Ddb=postgres -Dmaven.test.skip=true
