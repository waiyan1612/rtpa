package com.waiyan.rtcp.producer

import java.util.Properties
import java.util.concurrent.ExecutionException

import com.waiyan.rtcp.ser.{CarPark, CarParkRecord, CarParkSerializer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.LongSerializer

class CarParkProducer(brokerList: String) {
  case class KafkaProducerConfigs(brokerList: String = "127.0.0.1:9092") {
    val properties = new Properties()
    properties.put("bootstrap.servers", brokerList)
    properties.put("key.serializer", classOf[LongSerializer])
    properties.put("value.serializer", classOf[CarParkSerializer])
  }

  val producer = new KafkaProducer[Long, CarParkRecord](
    KafkaProducerConfigs(brokerList).properties)

  def produce(dt: String, topic: String, messages: Iterable[CarPark]): Unit = {
    messages.foreach { m =>
      try {
        val record = CarParkRecord(dt,
                                   m.id,
                                   m.development,
                                   m.location,
                                   m.availableLots,
                                   m.lotType)
        val metadata = producer
          .send(new ProducerRecord[Long, CarParkRecord](topic, record))
          .get
        println(
          "Record " + m + " sent to partition " + metadata.partition + " with offset " + metadata.offset)
      } catch {
        case e @ (_: ExecutionException | _: InterruptedException) => println(e)
      }
    }
  }
}
