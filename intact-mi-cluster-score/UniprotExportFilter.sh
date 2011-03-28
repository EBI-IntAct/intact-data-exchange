MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

mvn clean install -Puniprot-export-filter -Dsource=$1 -Dresults=$2 -Ddatabase=$3 -DbinaryOnly=$4 -DhighConfidence=$5 -DproteinOnly=$6 -DpositiveOnly=$7 -Dmaven.test.skip