#!/bin/bash

#SBATCH --time=06-00:00:00   # walltime
#SBATCH --ntasks=1   # number of tasks
#SBATCH --cpus-per-task=5   # number of CPUs Per Task i.e if your code is multi-threaded
#SBATCH -p research   # partition(s)
#SBATCH --mem=8G   # memory per node
#SBATCH -o "/nfs/production/hhe/intact/data/db-import-logs/import-evidence-%j.out"   # job output file
#SBATCH --mail-user=intact-dev@ebi.ac.uk   # email address
#SBATCH --mail-type=ALL

if [ $# == 4 ];
then
      INPUT_FILE=$1;
      USER_ID=$2;
      JOBID=$3;
      PROFILE=$4;
      echo "Input file: ${INPUT_FILE}"
      echo "User: ${USER_ID}"
      echo "Job id: ${JOBID}"
      echo "Profile: ${PROFILE}"
      mvn -U install -restart-import,${PROFILE} -Dmi.file=${INPUT_FILE} -Djami.user.context.id=${USER_ID} -Djob.name=interactionEvidenceImport -Derror.file=import_errors.log -Djob.id=${JOBID} -Dmaven.test.skip -Dmaven.repo.local=repository
else
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: INPUT_FILE USER_ID PROFILE"
      echo "usage: INPUT_FILE: the name of the file to enrich."
      echo "usage: USER_ID: the name of the user importing data"
      echo "usage: JOBID: the id of the job retrieved from the logs"
      echo "usage: PROFILE: the name of the profile"
      echo ""
      exit 1
fi