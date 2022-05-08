package com.waiyan.rtpa.producer.modules

import com.google.inject.AbstractModule
import com.typesafe.config.{Config, ConfigFactory}

class ProducerModule extends AbstractModule {

  override def configure(): Unit = {
    val config = ConfigFactory.load("producer.conf")
    bind(classOf[Config]).toInstance(config)
  }
}
