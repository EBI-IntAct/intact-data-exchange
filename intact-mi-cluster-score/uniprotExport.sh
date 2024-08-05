#!/bin/bash

#SBATCH --time=06-00:00:00   # walltime
#SBATCH --ntasks=1   # number of tasks
#SBATCH --cpus-per-task=5   # number of CPUs Per Task i.e if your code is multi-threaded
#SBATCH -p production   # partition(s)
#SBATCH --mem=4G   # memory per node
#SBATCH -J "UNIPROT_EXPORT_SCORING"   # job name
#SBATCH -o "/nfs/production/hhe/intact/data/uniprot-export-logs/uniprot-export-%j.out"   # job output file
#SBATCH --mail-user=intact-dev@ebi.ac.uk   # email address
#SBATCH --mail-type=ALL

MAVEN_OPTS="-Xms512m -Xmx4048m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

echo $1
echo $2
echo $3
echo $4
echo $5
echo $6
echo $7
echo $8
echo $9
echo $10
echo $11
echo $12

MAVEN_PROFILE=$7

echo "use profile ${MAVEN_PROFILE}"

mvn -U clean install -Puniprot-export,${MAVEN_PROFILE} -Drule=$1 -Dsource=$2 -Ddrfile=$3 -Dccfile=$4 -Dgofile=$5 -Dsilverccfile=$6 -DbinaryOnly=$8 -DhighConfidence=$9 -DproteinOnly=${10} -DpositiveOnly=${11} -DexcludeInferred=${12} -Dmaven.repo.local=repository -Dmaven.test.skip -Ddb=postgres
