MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

mvn clean install -Pbinary-export -Drule=$1 -Dmitab=$2 -DfileExported=$3 -DfileExcluded=$4 -Ddatabase=$5 -Dmaven.test.skip