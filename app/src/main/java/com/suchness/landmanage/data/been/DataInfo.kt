package com.suchness.landmanage.data.been
import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/23
 * 描述　: 账户信息
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class UserInfo(var token: String="",
                    var accessToken: String="") : Parcelable



data class trafficInfo(var recodId: String?,
                       var type: Int?,
                       var channel: String?,
                       var scene: String?,
                       var sort: String?,
                       var tab: String,
                       var plate: String,
                       var timeFrame: String,
                       var playBackBeginTime: String,
                       var playBackEndTime: String,
                       var averageSpeed: Double,
                       var maxSpeed: Double,
                       var serialNumber: String?,
                       var speedingRatio: Double,
                       var minSpeed: Double,
                       var imgPath: String?,
                       var longitude: String,
                       var latitude: String,
                       var altitude: String,
                       var track: String,
                       var coordinate: String,
                       var orderId: String,
                       var devDesc: String?,
                       var area: String?,
                       var alarmType: String,
                       var gplLocation: String?)

data class cityInfo(var orderId: Int?,
                    var id: Int?,
                    var imgPath: String?,
                    var type: Int?,
                    var startTime: String?,
                    var endTime: String?,
                    var channel: String?,
                    var track: String?,
                    var tab: String?,
                    var longitude: String?,
                    var latitude: String?,
                    var altitude: String?,
                    var area: String?,
                    var devDesc: String?,
                    var rtsp: String?,
                    var serialNumber: String?,
                    var number: String?,
                    var coordinate: String?,
                    var alarmType: String?,
                    var gplLocation: String?)

data class tifInfo(var fileName: String?, var id: Int?, var time: String?, var url: String?)

data class fileInfo(var id: Int,
                    var fileName: String,
                    var channelNumber: Int,
                    var alarmType: String,
                    var time: String,
                    var url: String)

data class deviceInfo(var id : Int?,
                      var devDesc : String?,
                      var channelNo : String?,
                      var serialNumber : String?,
                      var address : String?,
                      var devHigh : String?,
                      var deviceModel : String?,
                      var isOnline : String?,
                      var areaName : String?,
                      var lng : String?,
                      var lat : String?,
                      var picUrl: String?)

data class typeDict(
    var key: Int?,
    var value: String)

data class areaDict(var id: Int?,
                    var fullname: String)

data class requeryData(var type: Any?,
                        var area : String?,
                        var channel : String?,
                        var startTime : String?,
                        var endTime : String?)