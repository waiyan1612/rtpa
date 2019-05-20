package com.waiyan.rtpa.ser

import com.google.gson.Gson
import org.apache.kafka.common.serialization.Serializer

class CarParkSerializer extends Serializer[CarParkRecord] {
  private val gson: Gson = new Gson()
  override def configure(configs: java.util.Map[String, _],
                         isKey: Boolean): Unit = {
    // nothing to do
  }

  override def serialize(topic: String, data: CarParkRecord): Array[Byte] = {
    gson.toJson(data).getBytes
  }

  override def close(): Unit = {
    //nothing to do
  }
}
