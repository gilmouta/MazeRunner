#!/bin/bash

#CHANGE ME:
JDK7_PATH="/c/Program Files/Java/jdk1.7.0_80"

echo "# Setting java home to "$JDK7_PATH
export PATH=$JDK7_PATH/bin:${PATH}
export JAVA_HOME=$JDK7_PATH
echo $(java -version)
export MAVEN_OPTS="-XX:-UseSplitVerifier"
echo "# Done"

echo "# Compiling project"
mvn compile
echo "# Done"

echo "# Instrumenting project"
cd BIT
mvn exec:java
cd ..
echo "# Done"

echo "# Installing project"
mvn install
echo "# Done"

echo "# Running Webserver"
cd WebServer
mvn exec:java
cd ..
echo "# Done"