package com.waiyan.rtpa.util

import com.typesafe.config.ConfigFactory

import java.util.Properties
import com.typesafe.scalalogging.LazyLogging
import com.waiyan.rtpa.common.KafkaConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

import scala.collection.JavaConverters._

object ConnectionChecker extends LazyLogging {

  // from https://stackoverflow.com/a/47476774/3500885
  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.load("kafka.conf")
    val kafkaConfig = new KafkaConfig(config)

    val props = new Properties()
    props.put("bootstrap.servers", kafkaConfig.brokerList)
    props.put("key.deserializer", classOf[StringDeserializer])
    props.put("value.deserializer", classOf[StringDeserializer])
    val simpleConsumer = new KafkaConsumer[String, String](props)
    val topics = simpleConsumer.listTopics()
    logger.info("Listing topics ...")
    topics.asScala.keySet.foreach(x => logger.info(x))
    logger.info("Exiting ...")
  }
}
