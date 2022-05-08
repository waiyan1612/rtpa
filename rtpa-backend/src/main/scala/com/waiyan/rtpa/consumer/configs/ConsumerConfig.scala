package com.waiyan.rtpa.consumer.configs

import com.typesafe.config.Config

import javax.inject.{Inject, Singleton}

@Singleton
class ConsumerConfig @Inject()(conf: Config) extends Serializable {

  val triggerIntervalMinutes: Int = conf.getInt("consumer.trigger-interval-minutes")
  val outputPath: String = conf.getString("consumer.output-path")


}


