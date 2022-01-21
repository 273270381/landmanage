package com.suchness.deeplearningapp.app.network

import com.suchness.landmanage.data.been.*
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * @author: hejunfeng
 * @date: 2021/10/8 0008
 */
interface ApiService {
    companion object {
//        const val SERVER_URL = "http://172.16.14.8:6666/"
        const val SERVER_URL = "http://183.208.120.226:6666/api/"
    }

    /**
     * 登录
     */
    @FormUrlEncoded
    @POST("account/login")
    suspend fun login(@Field ("username") username : String,
                      @Field ("password") password : String): ApiResponse<UserInfo>


    /**
     * 文件查询
     */
    @FormUrlEncoded
    @POST("minio/getPicList")
    suspend fun getPicList(@Field("fileName") fileName: String?,
                           @Field("channelNum") channelNum: String?,
                           @Field("startTime") startTime: String?,
                           @Field("endTime") endTime: String?,
                           @Field("type") type: String?,
                           @Field("pageSize") pageSize: String?,
                           @Field("pageNum") pageNum: String?): ApiResponse<MutableList<fileInfo>>


    /**
     * 交通管控查询
     */
    @FormUrlEncoded
    @POST("alarm/s-traffic-control/list-paging")
    suspend fun strafficControlListPaging(@Field("type") type : Any?,
                                          @Field("area") area : String?,
                                          @Field("channelNum") channel : String?,
                                          @Field("startTime") startTime : String?,
                                          @Field("endTime") endTime : String?,
                                          @Field("pageSize") pageSize : Int?,
                                          @Field("pageNum") pageNum : Int?): ApiResponse<ResultInfo<trafficInfo>>


    /**
     * 治安管控查询
     */
    @FormUrlEncoded
    @POST("alarm/getAlarmMsg-police")
    suspend fun spoliceControlListPaging(@Field("type") type : Any?,
                                          @Field("area") area : String?,
                                          @Field("channelNum") channel : String?,
                                          @Field("startTime") startTime : String?,
                                          @Field("endTime") endTime : String?,
                                          @Field("pageSize") pageSize : Int?,
                                          @Field("pageNum") pageNum : Int?):ApiResponse<ResultInfo<cityInfo>>

    /**
     * 城市管理查询
     */
    @FormUrlEncoded
    @POST("alarm/getAlarmMsg-city")
    suspend fun scityControlListPaging(@Field("type") type : Any?,
                                       @Field("area") area : String?,
                                       @Field("channelNum") channel : String?,
                                       @Field("startTime") startTime : String?,
                                       @Field("endTime") endTime : String?,
                                       @Field("pageSize") pageSize : Int?,
                                       @Field("pageNum") pageNum : Int?):ApiResponse<ResultInfo<cityInfo>>


    /**
     * 设备查询
     */
    @FormUrlEncoded
    @POST("device/getAllDevice")
    suspend fun getAllDevice(@Field("areaName") areaName : String?,
                             @Field("pageSize") pageSize : Int?,
                             @Field("pageNum") pageNum : Int?):ApiResponse<ResultInfo<deviceInfo>>

    /**
     * 设备查询
     */
    @FormUrlEncoded
    @POST("device/getAllDevice")
    suspend fun getAllDeviceInfo(@Field("areaName") areaName : String?):ApiResponse<MutableList<deviceInfo>>


    /**
     * 交通管控字典查询
     */
    @POST("alarm/dict/list-traffic")
    suspend fun getTrafficDict():ApiResponse<MutableList<typeDict>>

    /**
     * 治安管控字典查询
     */
    @POST("alarm/dict/list-police")
    suspend fun getPoliceDict():ApiResponse<MutableList<typeDict>>

    /**
     * 城市管理字典查询
     */
    @POST("alarm/dict/list-city")
    suspend fun getCityDict():ApiResponse<MutableList<typeDict>>

    /**
     * 区域查询
     */
    @GET("apiUnity/areaApi/basicList")
    suspend fun getAreaDict():ApiResponse<MutableList<areaDict>>
}