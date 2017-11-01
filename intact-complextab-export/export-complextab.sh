#!/bin/sh

# Runs the complex export
#
# Usage $0 database folder
#
#
##################################################################

if [ $# -ne 3 ]; then
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: $0 DATABASE_PROFILE[ebi-test, ebi-prod, etc] FOLDER LOG_FILE"
      echo ""
      exit 1
fi

DATABASE=$1
FOLDER=$2
LOG=$3

rm -rf target
mkdir target

mvn clean install -Pcomplextab-export,${DATABASE},oracle -Dfolder=${FOLDER} -Dmaven.test.skip