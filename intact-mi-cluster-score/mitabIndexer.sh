MAVEN_OPTS="-Xms512m -Xmx4048m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

mvn -U clean install -Pmitab-indexer -Dmitab=$1 -Dclustered=$2 -Dmaven.test.skip
