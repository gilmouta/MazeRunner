#!/bin/bash

#CHANGE ME:
JDK7_PATH="/usr/lib/jvm/java"

echo "# Setting java home to "$JDK7_PATH
export PATH=$JDK7_PATH/bin:${PATH}
export JAVA_HOME=$JDK7_PATH
echo $(java -version)
export MAVEN_OPTS="-XX:-UseSplitVerifier"
echo "# Done"

echo "# Running Loadbalancer"
mvn -f /home/ec2-user/MazeRunner/LoadBalancer/pom.xml exec:java
echo "# Done"