package com.waiyan.rtpa.util

import java.util.Properties

import com.typesafe.scalalogging.Logger
import com.waiyan.rtpa.common.KafkaInfo
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import scala.collection.JavaConverters._

object ConnectionChecker {

  private val logger = Logger("com.waiyan.rtpa.util.ConnectionChecker")

  // from https://stackoverflow.com/a/47476774/3500885
  def main(args: Array[String]): Unit = {

    val props = new Properties()
    props.put("bootstrap.servers", KafkaInfo.BROKER_LIST)
    props.put("key.deserializer", classOf[StringDeserializer])
    props.put("value.deserializer", classOf[StringDeserializer])
    val simpleConsumer = new KafkaConsumer[String, String](props)
    val topics = simpleConsumer.listTopics()
    topics.asScala.keySet.foreach(x => logger.info(x))
  }

}
