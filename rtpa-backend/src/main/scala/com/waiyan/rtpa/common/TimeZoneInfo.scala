package com.waiyan.rtpa.common

import java.time.ZoneOffset
import java.util.TimeZone

object TimeZoneInfo {

  val timeZone = "Asia/Singapore"
  val zoneOffset: ZoneOffset = ZoneOffset.ofTotalSeconds(TimeZone.getTimeZone(timeZone).getRawOffset / 1000)
}
