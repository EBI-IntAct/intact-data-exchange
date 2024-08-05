#!/bin/bash

#if [ $# -ne 2 ]; then
#      echo ""
#      echo "ERROR: wrong number of parameters ($#)."
#      echo ""
#      exit 1
#fi

PROFILE=$1;

echo "Profile: $PROFILE"

mvn clean -U install -P import-orthology,${PROFILE} -Djob.name=orthologyImport -Dmaven.test.skip
