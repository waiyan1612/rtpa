#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd ${DIR}/../lib

if [ -z "$PRODUCER_JAVA_OPTS" ]
then
$PRODUCER_JAVA_OPTS="-Xmx512m"
fi

if [ -z "$CONSUMER_JAVA_OPTS" ]
then
$CONSUMER_JAVA_OPTS="-Xmx1g"
fi

mkdir -p ../logs
java $CONSUMER_JAVA_OPTS -cp spark-wrapper-assembly-0.0.1-SNAPSHOT.jar:rtcp-assembly-0.0.1-SNAPSHOT.jar com.waiyan.rtcp.consumer.ConsumerApp > ../logs/consumer.log 2>&1 &
java $PRODUCER_JAVA_OPTS -cp rtcp-assembly-0.0.1-SNAPSHOT.jar com.waiyan.rtcp.producer.ProducerApp > ../logs/producer.log 2>&1