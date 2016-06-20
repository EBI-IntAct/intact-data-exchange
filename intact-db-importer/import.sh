#!/bin/bash

if [ $# == 3 ];
then
      INPUT_FILE=$1;
      USER_ID=$2;
      PROFILE=$3;
      echo "Input file: ${INPUT_FILE}"
      echo "User: ${USER_ID}"
      echo "Profile: ${PROFILE}"
      mvn -U clean install -Pimport,${PROFILE} -Dmi.file=${INPUT_FILE} -Djami.user.context.id=${USER_ID} -Djob.name=interactionMixImport -Dmaven.repo.local=repository -Derror.file=import_errors.log -Dmaven.test.skip
else
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: INPUT_FILE USER_ID PROFILE"
      echo "usage: INPUT_FILE: the name of the file to enrich."
      echo "usage: USER_ID: the name of the user importing data"
      echo "usage: PROFILE: the name of the profile"
      echo ""
      exit 1
fi