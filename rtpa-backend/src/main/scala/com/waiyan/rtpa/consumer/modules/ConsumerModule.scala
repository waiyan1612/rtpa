package com.waiyan.rtpa.consumer.modules

import com.google.inject.AbstractModule
import com.typesafe.config.{Config, ConfigFactory}

class ConsumerModule extends AbstractModule {

  override def configure(): Unit = {
    val config = ConfigFactory.load("consumer.conf")
    bind(classOf[Config]).toInstance(config)
  }
}
