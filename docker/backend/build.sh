#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd $DIR/../../backend
mvn clean install
cd $DIR

cd $DIR/../../spark-wrapper
mvn clean install
cd $DIR

# Set up directories
BUILD_DIR=$DIR/../../backend/target
WRAPPER_DIR=$DIR/../../spark-wrapper/target
DOCKER_BUILD_DIR=$BUILD_DIR/docker
APP_DIR=$DOCKER_BUILD_DIR/app

JAR_NAME=$(cd $BUILD_DIR; ls rtcp-assembly*.jar)
NAME=$(echo $JAR_NAME | sed "s/^\(.*\)-\(.*\)-\(.*\)\.jar$/\1/")
VERSION=$(echo $JAR_NAME | sed "s/^\(.*\)-\(.*\)-\(.*\)\.jar$/\2/")

rm -rf $DOCKER_BUILD_DIR
mkdir -p $APP_DIR

# Copy required files
# conf folder should be mounted during the run time.
cp $DIR/Dockerfile $DOCKER_BUILD_DIR
mkdir -p $APP_DIR/bin/ && cp $DIR/../../backend/bin/run.sh $APP_DIR/bin/
mkdir -p $APP_DIR/lib/ && cp $BUILD_DIR/rtcp-assembly*.jar $APP_DIR/lib/
mkdir -p $APP_DIR/lib/ && cp $WRAPPER_DIR/spark-wrapper-assembly*.jar $APP_DIR/lib/

# Build docker image
cd $DOCKER_BUILD_DIR
#docker build -t $NAME:$VERSION -t $NAME .
docker build -t $NAME:$VERSION .

cd $DIR
rm -rf $DOCKER_BUILD_DIR