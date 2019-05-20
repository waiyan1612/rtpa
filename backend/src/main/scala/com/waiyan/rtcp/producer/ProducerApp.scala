package com.waiyan.rtcp.producer

import java.net.{HttpURLConnection, URL}

import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.{Executors, TimeUnit}

import com.waiyan.rtcp.common.KafkaInfo
import com.waiyan.rtcp.ser.{CarPark, JsonCarParkResp}

object ProducerApp {

  val gson = new Gson
  val apiOffset = 500

  def main(args: Array[String]): Unit = {
    val executor = Executors.newScheduledThreadPool(1)
    executor.scheduleAtFixedRate(process, 0L, 1L, TimeUnit.MINUTES)
  }

  private val process = new Runnable {
    override def run() = {
      var cursor = 0
      var count = 0
      var fetchedAll = false

      val now = LocalDateTime
        .now()
        .format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:00"))

      val brokerEnvVar = sys.env.get("KAFKA_BROKER_LIST")
      val brokers = if (brokerEnvVar.isDefined) {
        brokerEnvVar.get
      } else {
        KafkaInfo.BROKER_LIST
      }
      println(s"$now INFO: Using BROKER_LIST $brokers")
      val cp = new CarParkProducer(brokers)

      val apiKeyEnvVar = sys.env.get("DATAMALL_API_KEY")
      if (apiKeyEnvVar.isEmpty) {
        println(s"$now ERROR: DATAMALL_API_KEY environment variable not set.")
        System.exit(-1)
      }
      val apiKey = apiKeyEnvVar.get
      println(s"$now INFO: Using API KEY $apiKey")

      while (!fetchedAll) {
        val carParks = getCarParks(cursor, apiKey)
        cp.produce(now, KafkaInfo.TOPIC, carParks)
        fetchedAll = carParks.isEmpty
        count += carParks.length
        cursor += apiOffset
      }
      println(s"$now INFO: Fetched $count records.")
    }
  }

  private def getCarParks(cursor: Int, key: String) = {
    try {
      val content = get(
        "http://datamall2.mytransport.sg/ltaodataservice/CarParkAvailabilityv2?$skip=" + cursor,
        key)
      gson.fromJson(content, classOf[JsonCarParkResp]).value
    } catch {
      case e @ (_: java.io.IOException) =>
        println("Error occurred in getCarParks", e.getMessage)
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
      println("Error occurred while making a REST call.")
      println(scala.io.Source.fromInputStream(inputStream).mkString)
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
