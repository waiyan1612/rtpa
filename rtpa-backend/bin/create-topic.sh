#!/bin/bash

# Listing topics
/opt/bitnami/kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181

# Creating a new topic
/opt/bitnami/kafka/bin/kafka-topics.sh --create --zookeeper zookeeper:2181 --topic rtpa --replication-factor 1 --partitions 1

# Sending messages via Brokers
/opt/bitnami/kafka/bin/kafka-console-producer.sh --broker-list kafka:9092 --topic rtpa

# Receiving messages
/opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic rtpa --from-beginning
