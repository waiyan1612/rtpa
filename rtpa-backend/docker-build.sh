#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd $DIR

./gradlew clean shadowJar

JAR_NAME=$(cd $DIR/build/libs; ls rtpa-backend*.jar)
NAME=$(echo $JAR_NAME | sed "s/^\(.*\)-\(.*\)-\(.*\)\.jar$/\1/")
VERSION=$(echo $JAR_NAME | sed "s/^\(.*\)-\(.*\)-\(.*\)\.jar$/\2/")

docker build -t $NAME:$VERSION .
