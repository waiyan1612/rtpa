package com.waiyan.rtpa.common

import com.typesafe.config.Config

import javax.inject.{Inject, Singleton}

@Singleton
class KafkaConfig @Inject()(conf: Config) extends Serializable {

  val topic: String = conf.getString("topic")
  val brokerList: String = conf.getString("broker-list")

}


