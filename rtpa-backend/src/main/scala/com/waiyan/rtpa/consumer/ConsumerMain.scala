package com.waiyan.rtpa.consumer

import com.google.inject.Guice
import com.waiyan.rtpa.consumer.modules.ConsumerModule


object ConsumerMain {

  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(new ConsumerModule)
    val coordinator = injector.getInstance(classOf[ConsumerApp])
    coordinator.start()
  }
}
