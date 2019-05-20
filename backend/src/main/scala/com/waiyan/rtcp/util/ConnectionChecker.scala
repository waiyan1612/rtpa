package com.waiyan.rtcp.util

import java.util.Properties

import com.waiyan.rtcp.common.KafkaInfo
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

object ConnectionChecker {

  // from https://stackoverflow.com/a/47476774/3500885
  def main(args: Array[String]): Unit = {

    val props = new Properties()
    props.put("bootstrap.servers", KafkaInfo.BROKER_LIST)
    props.put("key.deserializer", classOf[StringDeserializer])
    props.put("value.deserializer", classOf[StringDeserializer])
    val simpleConsumer = new KafkaConsumer[String, String](props)
    val topics = simpleConsumer.listTopics()

    import scala.collection.JavaConversions._
    for ((k, _) <- topics) println(k)
  }

}
