#!/bin/bash

#SBATCH --time=06-00:00:00   # walltime
#SBATCH --ntasks=1   # number of tasks
#SBATCH --cpus-per-task=5   # number of CPUs Per Task i.e if your code is multi-threaded
#SBATCH -p production   # partition(s)
#SBATCH --mem=4G   # memory per node
#SBATCH -J "MUTATION_EXPORT"   # job name
#SBATCH -o "/nfs/production/hhe/intact/data/mutation_export/mutation-export-%j.out"   # job output file
#SBATCH --mail-user=intact-dev@ebi.ac.uk   # email address
#SBATCH --mail-type=ALL

# Runs the mutation update
#
# Usage $0 database folder
#
#
##################################################################

if [ $# -ne 3 ]; then
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: $0 DATABASE_PROFILE[ebi-test, ebi-prod, etc] FOLDER LOG_FILE"
      echo ""
      exit 1
fi

DATABASE=$1
FOLDER=$2
LOG=$3

rm -rf target
mkdir target

# Make sure we are using institution intact by default.
INTACT_OPTS="-Duk.ac.ebi.intact.INSTITUTION_LABEL=intact -Duk.ac.ebi.intact.AC_PREFIX=EBI"

mvn clean install -Pmutation-export,${DATABASE},oracle -Dfolder=${FOLDER} -Dmaven.test.skip=true