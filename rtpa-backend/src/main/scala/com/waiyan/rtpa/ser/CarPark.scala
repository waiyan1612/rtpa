package com.waiyan.rtpa.ser

import com.google.gson.annotations.SerializedName

case class CarPark(
    // @SerializedName alone doesn't work for case classes
    @(SerializedName @scala.annotation.meta.field)("CarParkID")
    id: String,
    //@(SerializedName @scala.annotation.meta.field)("Area")
    //area: String,
    //@(SerializedName @scala.annotation.meta.field)("Agency")
    //agency: String,
    @(SerializedName @scala.annotation.meta.field)("Development")
    development: String,
    @(SerializedName @scala.annotation.meta.field)("Location")
    location: String,
    @(SerializedName @scala.annotation.meta.field)("AvailableLots")
    availableLots: String,
    @(SerializedName @scala.annotation.meta.field)("LotType")
    lotType: String
)

case class JsonCarParkResp(
    value: Array[CarPark]
)

case class CarParkRecord(
    dt: String,
    id: String,
    development: String,
    location: String,
    availableLots: String,
    lotType: String
)
