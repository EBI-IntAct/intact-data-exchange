MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

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

mvn -U clean install -Puniprot-export,${MAVEN_PROFILE} -Drule=$1 -Dsource=$2 -Ddrfile=$3 -Dccfile=$4 -Dgofile=$5 -Dsilverccfile=$6 -DbinaryOnly=$8 -DhighConfidence=$9 -DproteinOnly=$10 -DpositiveOnly=${11} -DexcludeInferred=${12} -Dmaven.test.skip -Dmaven.repo.local=repository -Ddb=oracle
