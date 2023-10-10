#!/bin/bash

#SBATCH --time=06-00:00:00   # walltime
#SBATCH --ntasks=1   # number of tasks
#SBATCH --cpus-per-task=5   # number of CPUs Per Task i.e if your code is multi-threaded
#SBATCH -p research   # partition(s)
#SBATCH --mem=12G   # memory per node
#SBATCH -o "/nfs/production/hhe/intact/data/db-import-logs/import-folder-%j.out"   # job output file
#SBATCH --mail-user=intact-dev@ebi.ac.uk   # email address
#SBATCH --mail-type=ALL

TODAY=`date +%Y-%m-%d`

if [ $# == 3 ];
then
      FOLDER=$1;
      USER_ID=$2;
      PROFILE=$3;
      echo "User: ${USER_ID}"
      echo "Profile: ${PROFILE}"
      for FILE in ${FOLDER}/*.xml
      do
        echo "Input file: ${FILE}"
        rm -rf target
        echo "mvn -U clean install -Pimport,${PROFILE} -Dmi.file=${FILE} -Djami.user.context.id=${USER_ID} -Djob.name=interactionEvidenceImport -Dmaven.repo.local=repository -Derror.file=import_errors.log -Dmaven.test.skip >& logs/`basename ${FILE}`.import_${TODAY}.log"
        mvn -U clean install -Pimport,${PROFILE} -Dmi.file=${FILE} -Djami.user.context.id=${USER_ID} -Djob.name=interactionEvidenceImport -Dmaven.repo.local=repository -Derror.file=import_errors.log -Dmaven.test.skip >& logs/`basename ${FILE}`.import_${TODAY}.log
      done
else
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: FOLDER USER_ID PROFILE"
      echo "usage: FOLDER: the name of the folder with the files to enrich."
      echo "usage: USER_ID: the name of the user importing data"
      echo "usage: PROFILE: the name of the profile"
      echo ""
      exit 1
fi