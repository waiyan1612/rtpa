package com.waiyan.rtpa.producer

import java.net.{HttpURLConnection, URL}
import com.google.gson.Gson
import com.google.inject.{Inject, Singleton}

import java.time.{Duration, Instant, ZonedDateTime}
import java.time.temporal.ChronoUnit
import java.util.concurrent.{Executors, TimeUnit}
import com.typesafe.scalalogging.LazyLogging
import com.waiyan.rtpa.common.{KafkaConfig, TimeZoneInfo}
import com.waiyan.rtpa.producer.ProducerApp._
import com.waiyan.rtpa.producer.configs.{DataMallConfig, ProducerConfig}
import com.waiyan.rtpa.ser.{CarPark, JsonCarParkResp}

@Singleton
class ProducerApp @Inject()(
  kafkaConfig: KafkaConfig,
  producerConfig: ProducerConfig,
  dataMallConfig: DataMallConfig,
  gson: Gson,
) extends LazyLogging {

  def schedule(): Unit = {
    // Round up to closet second
    val now = ZonedDateTime.now(TimeZoneInfo.zoneOffset).truncatedTo(ChronoUnit.SECONDS).plusSeconds(1)
    val nextRun = now.truncatedTo(ChronoUnit.HOURS)
      .plusMinutes(producerConfig.triggerIntervalMinutes * (now.getMinute / producerConfig.triggerIntervalMinutes + 1).toLong)
    val initialDelay = Duration.between(now, nextRun).getSeconds

    logger.info(s"Process will start after $initialDelay seconds.")
    val executor = Executors.newScheduledThreadPool(1)
    executor.scheduleAtFixedRate(process, initialDelay, producerConfig.triggerIntervalMinutes * 60, TimeUnit.SECONDS)
  }

  private val process = new Runnable {
    override def run(): Unit = {
      var cursor = 0
      var count = 0
      var fetchedAll = false

      val carParkProducer = new CarParkProducer(kafkaConfig.brokerList)
      val now =  ZonedDateTime.now(TimeZoneInfo.zoneOffset)
      val minToAdd = producerConfig.triggerIntervalMinutes * Math.round(now.getMinute.toDouble / producerConfig.triggerIntervalMinutes)
      val roundedNow = Instant.from(now).truncatedTo(ChronoUnit.HOURS).plus(minToAdd, ChronoUnit.MINUTES)

      while (!fetchedAll) {
        val carParks = getCarParks(cursor)
        carParkProducer.produce(roundedNow.toString, kafkaConfig.topic, carParks)
        fetchedAll = carParks.isEmpty
        count += carParks.length
        cursor += API_OFFSET
      }
      logger.info(s"Fetched $count records.")
    }
  }

  private def getCarParks(cursor: Int) = {
    try {
      val content = get(cursor)
      gson.fromJson(content, classOf[JsonCarParkResp]).value
    } catch {
      case e @ (_: java.io.IOException) =>
        logger.error("Error occurred in getCarParks", e)
        Array[CarPark]()
    }
  }

  @throws(classOf[java.io.IOException])
  private def get(cursor: Int): String = {
    val connection = new URL(s"$CARPARK_ENDPOINT?$$skip=$cursor").openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestProperty("AccountKey", dataMallConfig.apiKey)
    connection.setConnectTimeout(5000)
    connection.setReadTimeout(5000)
    connection.setRequestMethod("GET")
    val errStream = connection.getErrorStream
    val inputStream = connection.getInputStream
    if (errStream != null) {
      logger.error("Error occurred while making a REST call.")
      logger.error(scala.io.Source.fromInputStream(inputStream).mkString)
    }
    if (inputStream != null) {
      val content = scala.io.Source.fromInputStream(inputStream).mkString
      inputStream.close()
      content
    } else {
      throw new java.io.IOException("Null input stream.")
    }
  }
}

object ProducerApp {
  val CARPARK_ENDPOINT: String = "http://datamall2.mytransport.sg/ltaodataservice/CarParkAvailabilityv2"
  val API_OFFSET = 500
}