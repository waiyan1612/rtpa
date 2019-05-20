package com.waiyan.rtcp.consumer

import java.util.concurrent.TimeUnit

import com.waiyan.rtcp.common.KafkaInfo
import com.waiyan.rtcp.ser.{CarParkDeserializer, CarParkRecord}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger

object ConsumerApp {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .master("local")
      .appName(this.getClass.getName)
      .getOrCreate()

    val brokerEnvVar = sys.env.get("KAFKA_BROKER_LIST")
    val brokers = if (brokerEnvVar.isDefined) {
      brokerEnvVar.get
    } else {
      KafkaInfo.BROKER_LIST
    }

    val txnStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", KafkaInfo.TOPIC)
      .load()

    object dsWrapper {
      val deser = new CarParkDeserializer
    }

    val deserializeUdf = udf { bytes: Array[Byte] =>
      dsWrapper.deser.deserialize(KafkaInfo.TOPIC, bytes)
    }

    import spark.implicits._
    val cpDf = txnStream
      .select(deserializeUdf(col("value")).as("cp"))
      .select("cp.dt",
              "cp.id",
              "cp.development",
              "cp.location",
              "cp.availableLots",
              "cp.lotType")
      .as[CarParkRecord]
      .withColumn("_tmp", split(col("location"), " "))
      .withColumn("lat", $"_tmp".getItem(0))
      .withColumn("lon", $"_tmp".getItem(1))
      .drop("_tmp", "location")

    val rtcpCsvEnvVar = sys.env.get("RTCP_CSV_PATH")
    if (rtcpCsvEnvVar.isEmpty) {
      println(s"ERROR: RTCP_CSV_PATH environment variable not set.")
      System.exit(-1)
    }
    val outputDir = rtcpCsvEnvVar.get

//    val motorcycleDf = cpDf.filter(col("lotType") === "Y").drop("lotType")
//    val q1 = motorcycleDf.writeStream
//      .outputMode("append")
//      .trigger(Trigger.ProcessingTime(1, TimeUnit.MINUTES))
//      .format("csv")
//      .option("path", "data/motorcycles")
//      .option("checkpointLocation", "cp/motorcycles")
//      .start

    val carDf = cpDf.filter(col("lotType") =!= "Y").drop("lotType")
    val q2 = carDf.writeStream
      .outputMode("append")
      .trigger(Trigger.ProcessingTime(1, TimeUnit.MINUTES))
      .format("csv")
      .option("path", outputDir)
      .option("checkpointLocation", "cp/cars")
      .start

    spark.streams.awaitAnyTermination
  }

}
