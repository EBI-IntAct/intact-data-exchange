#!/bin/bash

if [ $# == 2 ];
then
      PROFILE=$1;
      OUTPUT_FILE=$2;
      echo "Profile: ${PROFILE}"
      echo "Input file: ${OUTPUT_FILE}"
      mvn -U clean install -Pexport,${PROFILE} -Dtarget.file=${OUTPUT_FILE} -Dmaven.test.skip
else
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: PROFILE OUTPUT_FILE"
      echo "usage: PROFILE: the name of the profile"
      echo "usage: OUTPUT_FILE: the name of the file to enrich."
      echo ""
      exit 1
fi