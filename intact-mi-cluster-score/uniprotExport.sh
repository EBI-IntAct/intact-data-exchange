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

MAVEN_PROFILE=$6

mvn -U clean install -Puniprot-export,${MAVEN_PROFILE} -Drule=$1 -Dsource=$2 -Ddrfile=$3 -Dccfile=$4 -Dgofile=$5 -DbinaryOnly=$7 -DhighConfidence=$8 -DproteinOnly=$9 -DpositiveOnly=${10} -DexcludeInferred=${11} -Dmaven.test.skip -Dmaven.repo.local=repository -Ddb=oracle
