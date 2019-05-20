package com.waiyan.rtpa.ser

import com.google.gson.Gson
import org.apache.kafka.common.serialization.Deserializer

class CarParkDeserializer extends Deserializer[CarParkRecord] {
  private val gson: Gson = new Gson()
  override def configure(configs: java.util.Map[String, _],
                         isKey: Boolean): Unit = {
    // nothing to do
  }

  override def deserialize(topic: String, data: Array[Byte]): CarParkRecord = {
    gson.fromJson(new String(data), classOf[CarParkRecord])
  }

  override def close(): Unit = {
    //nothing to do
  }
}
