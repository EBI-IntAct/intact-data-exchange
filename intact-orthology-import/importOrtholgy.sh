#!/bin/bash

#SBATCH --time=02-00:00:00   # walltime
#SBATCH --ntasks=1   # number of tasks
#SBATCH --cpus-per-task=5   # number of CPUs Per Task i.e if your code is multi-threaded
#SBATCH -p research   # partition(s)
#SBATCH --mem=12G   # memory per node
#SBATCH -J "ORTHOLOG_IMPORT"   # job name
#SBATCH -o "/nfs/production/hhe/intact/data/panther/logs/ortholog-import-%j.out"   # job output file
#SBATCH --mail-type=ALL
#SBATCH --mail-user= susiehuget@ebi.ac.uk   # email address
export JAVA_HOME=/hps/software/users/hhe/intact/third-party-softwares/latest_intact_jdk11

#if [ $# -ne 2 ]; then
#      echo ""
#      echo "ERROR: wrong number of parameters ($#)."
#      echo ""
#      exit 1
#fi

PROFILE=$1;

echo "Profile: $PROFILE"

mvn clean -U install -P import-orthology,${PROFILE} -Djob.name=orthologyImport -Dmaven.test.skip