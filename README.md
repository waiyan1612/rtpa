# RTCP

## Components

1. **Kafka with ZooKeeper** - handles data streams
2. **Kafka Producer** - fetches data from DataMall
3. **Kafka Consumer** - stream the data from Kafka and stores as csv
4. **NodeJs Converter** - transforms csv data to geojson
5. **Nginx Web UI with MapBox** - renders the geojson

## Docker Images

1. **bitnami-kafka**
2. **bitnami-zookeeper**
2. **rtcp-assembly** - Kafka Producer + Consumer
2. **rtcp-converter** - NodeJS Converter
2. **rtcp-ui** - Mapbox based HTML served from Nginx server

Since bitnami kafka image doesn't provide a way to create topics on start up, we have to start zookeeper and kafka first and manually create the topic.

    $ docker-compose -f docker-compose-kafka.yml up -d
    $ docker container exec -it rtcp_kafka_1 /bin/bash
    $ /opt/bitnami/kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181 
    $ /opt/bitnami/kafka/bin/kafka-topics.sh --create --zookeeper zookeeper:2181 --topic rtcp --replication-factor 1 --partitions 1
    $ docker-compose up -d

As a side effect, we have to run both docker-compose files everytime.