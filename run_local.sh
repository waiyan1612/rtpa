#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd $DIR

# Shut down existing containers
docker-compose -f docker-compose-local.yml down
docker-compose -f docker-compose-kafka.yml down

./docker-build.sh

# Spawn kafka containers and wait 10s for them to be ready
docker-compose -f docker-compose-kafka.yml up -d
sleep 10

# Create rtpa topic
docker container exec rtpa_kafka_1 /opt/bitnami/kafka/bin/kafka-topics.sh --create --zookeeper zookeeper:2181 --topic rtpa --replication-factor 1 --partitions 1

# Spawn rtpa containers
docker-compose -f docker-compose-local.yml up
