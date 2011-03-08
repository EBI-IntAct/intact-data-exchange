MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

mvn clean install -Puniprot-export -Drule=$1 -Dsource=$2 -Ddrfile=$3 -Dccfile=$4 -Dgofile=$5 -Ddatabase=$6 -DbinaryOnly=$7 -DhighConfidence=$8 -DproteinOnly=$9 -DpositiveOnly=$10 -Dmaven.test.skip
