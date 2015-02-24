#!/bin/bash

if [ $# == 4 ];
then
      RELEASE_FOLDER=$1;
      BUILD_FOLDER=$2;
      JOBID=$3;
      PROFILE=$4;
      echo "Release folder: ${RELEASE_FOLDER}"
      echo "Build folder: ${BUILD_FOLDER}"
      echo "Job id: ${JOBID}"
      echo "Profile: ${PROFILE}"
      mvn -U clean install -Prestart-export-complex-xml,${PROFILE} -Drelease.folder=${RELEASE_FOLDER} -Drelease.buildFolder=${BUILD_FOLDER}
      -Dmaven.test.skip -Dmaven.repo.local=repository -Djob.id=${JOBID}

else
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: RELEASE_FOLDER BUILD_FOLDER JOBID PROFILE"
      echo "Release folder: ${RELEASE_FOLDER}"
      echo "Build folder: ${BUILD_FOLDER}"
      echo "usage: JOBID: the id of the job retrieved from the logs"
      echo "usage: PROFILE: the name of the profile"
      echo ""
      exit 1
fi