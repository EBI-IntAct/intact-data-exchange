MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

mvn clean install -Psort-mi-score -Dfile1=$1 -Dfile2=$2 -Dfile3=$3 -Dmaven.test.skip