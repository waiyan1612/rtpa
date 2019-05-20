package com.waiyan.rtpa.producer

import java.util.Properties
import java.util.concurrent.ExecutionException

import com.typesafe.scalalogging.Logger
import com.waiyan.rtpa.ser.{CarPark, CarParkRecord, CarParkSerializer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.LongSerializer

class CarParkProducer(brokerList: String) {

  case class KafkaProducerConfigs(brokerList: String = "127.0.0.1:9092") {
    val properties = new Properties()
    properties.put("bootstrap.servers", brokerList)
    properties.put("key.serializer", classOf[LongSerializer])
    properties.put("value.serializer", classOf[CarParkSerializer])
  }

  private val logger = Logger[CarParkProducer]

  private val producer = new KafkaProducer[Long, CarParkRecord](
    KafkaProducerConfigs(brokerList).properties)

  def produce(dt: String, topic: String, messages: Iterable[CarPark]): Unit = {
    messages.foreach { m =>
      try {
        val record = CarParkRecord(dt, m.id, m.development, m.location, m.availableLots, m.lotType)
        val metadata = producer.send(new ProducerRecord[Long, CarParkRecord](topic, record)).get
        logger.debug(s"Record $m sent to partition ${metadata.partition} with offset ${metadata.offset}")
      } catch {
        case e @ (_: ExecutionException | _: InterruptedException) => logger.error(e.getMessage, e)
      }
    }
  }
}
