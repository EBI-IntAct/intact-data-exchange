#!/bin/sh

# Runs the complex export
#
# Usage $0 folder
#
#
##################################################################

if [ $# -ne 2 ]; then
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: $0 DATABASE FOLDER"
      echo ""
      exit 1
fi


DATABASE=$1
FOLDER=$2

rm -rf target
mkdir target

mvn clean install -Pgpa-export,${DATABASE},oracle -Dfolder=${FOLDER} -Dmaven.test.skip
mvn clean install -Pgpi-export,${DATABASE},oracle -Dfolder=${FOLDER} -Dmaven.test.skip
