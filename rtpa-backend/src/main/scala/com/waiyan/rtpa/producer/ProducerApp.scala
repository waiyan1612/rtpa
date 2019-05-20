package com.waiyan.rtpa.producer

import java.net.{HttpURLConnection, URL}

import com.google.gson.Gson
import java.time.{Duration, Instant, ZonedDateTime}
import java.time.temporal.{ChronoField, ChronoUnit}
import java.util.concurrent.{Executors, TimeUnit}

import com.typesafe.scalalogging.Logger
import com.waiyan.rtpa.common.{KafkaInfo, TimeZoneInfo}
import com.waiyan.rtpa.ser.{CarPark, JsonCarParkResp}

object ProducerApp {

  private val gson = new Gson
  private val apiOffset = 500
  private val logger = Logger("com.waiyan.rtpa.producer.ProducerApp")

  private val DEFAULT_PRODUCER_TRIGGER_INTERVAL_MINUTES = "5"
  private val producerTriggerInterval: Int = sys.env.getOrElse("PRODUCER_TRIGGER_INTERVAL_MINUTES", DEFAULT_PRODUCER_TRIGGER_INTERVAL_MINUTES).toInt

  def main(args: Array[String]): Unit = {

    // Round up to closet second
    val now = ZonedDateTime.now(TimeZoneInfo.zoneOffset).truncatedTo(ChronoUnit.SECONDS).plusSeconds(1)
    val nextRun = now.truncatedTo(ChronoUnit.HOURS).plusMinutes(producerTriggerInterval * (now.getMinute / producerTriggerInterval + 1).toLong)
    val initialDelay = Duration.between(now, nextRun).getSeconds

    val executor = Executors.newScheduledThreadPool(1)
    executor.scheduleAtFixedRate(process, initialDelay, producerTriggerInterval * 60, TimeUnit.SECONDS)
  }

  private val process = new Runnable {
    override def run() = {
      var cursor = 0
      var count = 0
      var fetchedAll = false

      val carParkProducer = new CarParkProducer(sys.env.getOrElse("KAFKA_BROKER_LIST", KafkaInfo.BROKER_LIST))
      val apiKey = sys.env.getOrElse("DATAMALL_API_KEY", throw new RuntimeException("DATAMALL_API_KEY is unset"))

      val now =  ZonedDateTime.now(TimeZoneInfo.zoneOffset)
      val minToAdd = producerTriggerInterval * Math.round(now.getMinute.toDouble / producerTriggerInterval)
      val roundedNow = Instant.from(now).truncatedTo(ChronoUnit.HOURS).plus(minToAdd, ChronoUnit.MINUTES)

      while (!fetchedAll) {
        val carParks = getCarParks(cursor, apiKey)
        carParkProducer.produce(roundedNow.toString, KafkaInfo.TOPIC, carParks)
        fetchedAll = carParks.isEmpty
        count += carParks.length
        cursor += apiOffset
      }
      logger.info(s"Fetched $count records.")
    }
  }

  private def getCarParks(cursor: Int, key: String) = {
    try {
      val content = get(s"http://datamall2.mytransport.sg/ltaodataservice/CarParkAvailabilityv2?$$skip=$cursor", key)
      gson.fromJson(content, classOf[JsonCarParkResp]).value
    } catch {
      case e @ (_: java.io.IOException) =>
        logger.error("Error occurred in getCarParks", e)
        Array[CarPark]()
    }
  }

  @throws(classOf[java.io.IOException])
  private def get(url: String, accountKey: String) = {
    val connection = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestProperty("AccountKey", accountKey)
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
