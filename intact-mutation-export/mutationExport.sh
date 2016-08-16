#!/bin/sh

# Runs the protein update
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
LOG=$4

rm -rf target
mkdir target

# Make sure we are using institution intact by default.
INTACT_OPTS="-Duk.ac.ebi.intact.INSTITUTION_LABEL=intact -Duk.ac.ebi.intact.AC_PREFIX=EBI"

mvn clean -U -X install -Pmutation-export,${DATABASE},oracle -Dfolder=${FOLDER} -Ddb=oracle -Dmaven.test.skip -Dmaven.repo.local=repository