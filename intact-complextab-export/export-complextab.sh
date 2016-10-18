#!/bin/sh

# Runs the protein update
#
# Usage $0 database folder
#
#
##################################################################

if [ $# -ne 1 ]; then
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: $0 DATABASE_PROFILE[ebi-test, ebi-prod, etc] FOLDER LOG_FILE"
      echo ""
      exit 1
fi

FOLDER=$2

rm -rf target
mkdir target

mvn clean install -Pcomplextab-export,ebi-release,oracle -Dfolder=${FOLDER} -Dmaven.test.skip