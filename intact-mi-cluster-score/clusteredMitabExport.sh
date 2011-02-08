MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

mvn -U clean install -Pbinary-export -Drule=$1 -Dmitab=$2 -Dlog=$3 -Ddatabase=$4 -Dmaven.test.skip