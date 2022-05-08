package com.waiyan.rtpa.consumer

import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging

import java.util.concurrent.TimeUnit
import com.waiyan.rtpa.common.{KafkaConfig, TimeZoneInfo}
import com.waiyan.rtpa.consumer.configs.ConsumerConfig
import com.waiyan.rtpa.ser.{CarParkDeserializer, CarParkRecord}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger

@Singleton
class ConsumerApp @Inject()(
  kafkaConfig: KafkaConfig,
  consumerConfig: ConsumerConfig
) extends Serializable with LazyLogging {

  def start(): Unit = {

    val spark = SparkSession
      .builder()
      .master("local")
      .appName(this.getClass.getName)
      .getOrCreate()

    // Set TimeZone to Asia/Singapore since the data does not include timezone information
    spark.conf.set("spark.sql.session.timeZone", TimeZoneInfo.timeZone)

    val txnStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.brokerList)
      .option("subscribe", kafkaConfig.topic)
      .load()

    object dsWrapper {
      val deser = new CarParkDeserializer
    }

    val deserializeUdf = udf { bytes: Array[Byte] =>
      dsWrapper.deser.deserialize(kafkaConfig.topic, bytes)
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

    val carDf = cpDf.filter(col("lotType") =!= "Y").drop("lotType")
    carDf.writeStream
      .outputMode("append")
      .trigger(Trigger.ProcessingTime(consumerConfig.triggerIntervalMinutes, TimeUnit.MINUTES))
      .partitionBy("_dt")
      .format("csv")
      .option("path", s"${consumerConfig.outputPath}/cars")
      .option("checkpointLocation", s"${consumerConfig.outputPath}/../checkpoints/cars")
      .start

    spark.streams.awaitAnyTermination
  }

}
