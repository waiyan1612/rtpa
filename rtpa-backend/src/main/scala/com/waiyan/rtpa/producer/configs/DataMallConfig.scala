package com.waiyan.rtpa.producer.configs

import com.typesafe.config.Config

import javax.inject.{Inject, Singleton}

@Singleton
class DataMallConfig @Inject()(conf: Config) {

  val apiKey: String = conf.getString("datamall.api-key")
  require(apiKey.nonEmpty, "DataMall API Key is not configured.")
}


