package com.waiyan.rtpa.producer

import com.google.inject.Guice
import com.waiyan.rtpa.producer.modules.ProducerModule

object ProducerMain {

  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(new ProducerModule)
    val coordinator = injector.getInstance(classOf[ProducerApp])
    coordinator.schedule()
  }
}
