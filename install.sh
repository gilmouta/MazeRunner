#!/bin/bash

#CHANGE ME:
JDK7_PATH="/c/Program Files/Java/jdk1.7.0_80"
AWS_SDK_PATH="/c/Users/gilmo/Documents/Aulas/4A2S/CNV/aws-java-sdk-1.11.315"

echo "# Setting java home to "$JDK7_PATH
export PATH=$JDK7_PATH/bin:${PATH}
export JAVA_HOME=$JDK7_PATH
echo $(java -version)
export MAVEN_OPTS="-XX:-UseSplitVerifier"
echo "# Done"

echo "# Compiling project"
mvn compile -Daws.sdk.path=$AWS_SDK_PATH
echo "# Done"

echo "# Instrumenting project"
cd BIT
mvn exec:java -Daws.sdk.path=$AWS_SDK_PATH
cd ..
echo "# Done"

echo "# Installing project"
mvn install -Daws.sdk.path=$AWS_SDK_PATH
echo "# Done"

echo "# Running Webserver"
cd WebServer
mvn exec:java -Daws.sdk.path=$AWS_SDK_PATH
cd ..
echo "# Done"