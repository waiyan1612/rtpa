#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $DIR

if [ -z "$PRODUCER_JAVA_OPTS" ]
then
$PRODUCER_JAVA_OPTS="-Xmx512m"
fi

if [ -z "$CONSUMER_JAVA_OPTS" ]
then
$CONSUMER_JAVA_OPTS="-Xmx1g"
fi

mkdir -p ../logs
java $CONSUMER_JAVA_OPTS -cp rtpa-backend-1.0.0-all.jar com.waiyan.rtpa.consumer.ConsumerApp 2>&1 &
java $PRODUCER_JAVA_OPTS -cp rtpa-backend-1.0.0-all.jar com.waiyan.rtpa.producer.ProducerApp 2>&1
