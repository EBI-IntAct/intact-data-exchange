#!/bin/bash

if [ $# == 3 ];
then
      PROFILE=$1;
      OUTPUT_FILE=$2;
      JOB_ID=$3
      echo "Profile: ${PROFILE}"
      echo "Input file: ${OUTPUT_FILE}"
      echo "Job id: ${JOB_ID}"
      mvn -U clean install -Prestart-export,${PROFILE} -Dtarget.file=${OUTPUT_FILE} -Djob.id=${JOB_ID} -Dmaven.test.skip
else
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: PROFILE OUTPUT_FILE JOB_ID"
      echo "usage: PROFILE: the name of the profile"
      echo "usage: OUTPUT_FILE: the name of the file to enrich."
      echo "usage: JOB_ID: the job id"
      echo ""
      exit 1
fi