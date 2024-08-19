#!/bin/bash

# Runs the complex export
#
# Usage $0 database file_name_prefix released
#
#
##################################################################

if [ $# -ne 2 ]; then
      echo ""
      echo "ERROR: wrong number of parameters ($#)."
      echo "usage: $0 DATABASE FILE_NAME_PREFIX RELEASED"
      echo ""
      exit 1
fi


DATABASE=$1
FILE_NAME_PREFIX=$2
RELEASED=$3
PREDICTED=$4

rm -rf target
mkdir target

mvn clean install -Pcomplex-pdb-export,${DATABASE},postgres -Dprefix=${FILE_NAME_PREFIX} -Dreleased=${RELEASED} -Dpredicted=${PREDICTED} -Dmaven.test.skip=true
