package com.suchness.landmanage.viewmodel

import android.app.ProgressDialog
import androidx.lifecycle.MutableLiveData
import com.suchness.deeplearningapp.app.network.apiService
import com.suchness.landmanage.data.been.*
import me.hgj.jetpackmvvm.base.viewmodel.BaseViewModel
import me.hgj.jetpackmvvm.ext.request
import me.hgj.jetpackmvvm.state.ResultState

/**
 * @author: hejunfeng
 * @date: 2021/12/13 0013
 */
class AlarmViewModel : BaseViewModel() {
    //页码
    var pageNum = 1

    var trafficAlramInfo = MutableLiveData<ResultState<ResultInfo<trafficInfo>>>()

    var policeAlramInfo = MutableLiveData<ResultState<ResultInfo<cityInfo>>>()

    var cityAlramInfo = MutableLiveData<ResultState<ResultInfo<cityInfo>>>()

    var typeList = MutableLiveData<ResultState<MutableList<typeDict>>>()

    var areaList = MutableLiveData<ResultState<MutableList<areaDict>>>()

    var deviceList = MutableLiveData<ResultState<MutableList<deviceInfo>>>()

    fun getData(cid: String, data: requeryData){
        when(cid){
            "交通管控" -> {
                request({ apiService.strafficControlListPaging(data.type,data.area,data.channel,data.startTime,data.endTime,20,pageNum)},trafficAlramInfo,true)
            }
            "治安管控" -> {
                request({ apiService.spoliceControlListPaging(data.type,data.area,data.channel,data.startTime,data.endTime,20,pageNum)},policeAlramInfo,true)
            }
            "城市管理" -> {
                request({ apiService.scityControlListPaging(data.type,data.area,data.channel,data.startTime,data.endTime,20,pageNum)},cityAlramInfo,true)
            }
        }
    }

    fun getDict(cid: String){
        when(cid){
            "交通管控" -> {
                request({ apiService.getTrafficDict()},typeList,false)
                request({ apiService.getAreaDict()},areaList,false)
                request({ apiService.getAllDeviceInfo(null)},deviceList,false)
            }
            "治安管控" -> {
                request({ apiService.getPoliceDict()},typeList,false)
                request({ apiService.getAreaDict()},areaList,false)
                request({ apiService.getAllDeviceInfo(null)},deviceList,false)
            }
            "城市管理" -> {
                request({ apiService.getCityDict()},typeList,false)
                request({ apiService.getAreaDict()},areaList,false)
                request({ apiService.getAllDeviceInfo(null)},deviceList,false)
            }
        }
    }
}