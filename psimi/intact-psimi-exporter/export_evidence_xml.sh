#!/bin/bash

if [ $# == 3 ];
then
      RELEASE_FOLDER=$1;
      BUILD_FOLDER=$2;
      PROFILE=$3;
      echo "Release folder: ${RELEASE_FOLDER}"
      echo "Build folder: ${BUILD_FOLDER}"
      echo "Profile: ${PROFILE}"
      mvn -U clean install -Pexport-evidence-xml,${PROFILE} -Drelease.folder=${RELEASE_FOLDER} -Drelease.buildFolder=${BUILD_FOLDER} -Dmaven.test.skip -Dmaven.repo.local=repository
else
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: RELEASE_FOLDER BUILD_FOLDER PROFILE"
      echo "usage: RELEASE_FOLDER: the name of the folder where to export files."
      echo "usage: BUILD_FOLDER: the name of the folder where to export temporary build files"
      echo "usage: PROFILE: the name of the profile"
      echo ""
      exit 1
fi