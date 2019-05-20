package com.waiyan.rtpa.consumer

import java.util.concurrent.TimeUnit

import com.waiyan.rtpa.common.{KafkaInfo, TimeZoneInfo}
import com.waiyan.rtpa.ser.{CarParkDeserializer, CarParkRecord}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger

object ConsumerApp extends Serializable {

  private val DEFAULT_RTPA_CSV_PATH = "/tmp/rtpa/data/csv"
  private val DEFAULT_CONSUMER_TRIGGER_INTERVAL_MINUTES = "5"

  def main(args: Array[String]): Unit = {

    val consumerTriggerInterval: Long = sys.env.getOrElse("CONSUMER_TRIGGER_INTERVAL_MINUTES", DEFAULT_CONSUMER_TRIGGER_INTERVAL_MINUTES).toLong

    val spark = SparkSession
      .builder()
      .master("local")
      .appName(this.getClass.getName)
      .getOrCreate()

    // Set TimeZone to Asia/Singapore since the data does not include timezone information
    spark.conf.set("spark.sql.session.timeZone", TimeZoneInfo.timeZone)

    val brokers = sys.env.getOrElse("KAFKA_BROKER_LIST", KafkaInfo.BROKER_LIST)

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
      // Note: _dt will use the timezone provided by spark.sql.session.timeZone
      .withColumn("_dt", date_format(to_timestamp($"dt"), "yyyyMMdd_HHmm"))

    val outputDir = sys.env.getOrElse("RTPA_CSV_PATH", DEFAULT_RTPA_CSV_PATH)

//    val motorcycleDf = cpDf.filter(col("lotType") === "Y").drop("lotType")
//    motorcycleDf.writeStream
//      .outputMode("append")
//      .trigger(Trigger.ProcessingTime(consumerTriggerInterval, TimeUnit.MINUTES))
//      .partitionBy("_dt")
//      .format("csv")
//      .option("path", s"$outputDir/motorcycles")
//      .option("checkpointLocation", "checkpoints/motorcycles")
//      .start

    val carDf = cpDf.filter(col("lotType") =!= "Y").drop("lotType")
    carDf.writeStream
      .outputMode("append")
      .trigger(Trigger.ProcessingTime(consumerTriggerInterval, TimeUnit.MINUTES))
      .partitionBy("_dt")
      .format("csv")
      .option("path", s"$outputDir/cars")
      .option("checkpointLocation", s"$outputDir/../checkpoints/cars")
      .start

    spark.streams.awaitAnyTermination
  }

}
