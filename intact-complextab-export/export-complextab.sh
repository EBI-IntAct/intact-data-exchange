#!/bin/sh

# Runs the protein update
#
# Usage $0 folder
#
#
##################################################################

if [ $# -ne 1 ]; then
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: $0 FOLDER"
      echo ""
      exit 1
fi

FOLDER=$1

rm -rf target
mkdir target

mvn clean install -Pcomplextab-export,ebi-release,oracle -Dfolder=${FOLDER} -Dmaven.test.skip