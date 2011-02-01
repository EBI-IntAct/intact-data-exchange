MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

mvn -U clean install -Puniprot-export -Drule=$1 -Dsource=$2 -Ddrfile=$3 -Dccfile=$4 -Dgofile=$5 -Dmaven.test.skip