package com.waiyan.rtpa.producer.configs

import com.typesafe.config.Config

import javax.inject.{Inject, Singleton}

@Singleton
class ProducerConfig @Inject()(conf: Config) {

  val triggerIntervalMinutes: Int = conf.getInt("producer.trigger-interval-minutes")

}


