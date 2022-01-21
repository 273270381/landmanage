package com.suchness.landmanage.viewmodel

import androidx.lifecycle.MutableLiveData
import com.suchness.deeplearningapp.app.network.apiService
import com.suchness.landmanage.data.been.ResultInfo
import com.suchness.landmanage.data.been.cityInfo
import com.suchness.landmanage.data.been.deviceInfo
import com.suchness.landmanage.data.been.trafficInfo
import me.hgj.jetpackmvvm.base.viewmodel.BaseViewModel
import me.hgj.jetpackmvvm.ext.request
import me.hgj.jetpackmvvm.state.ResultState

/**
 * @author: hejunfeng
 * @date: 2021/12/20 0020
 */
class MonitorViewModel : BaseViewModel(){

    //页码
    var pageNum = 1

    var devInfo = MutableLiveData<ResultState<ResultInfo<deviceInfo>>>()

    fun getData(){
        request({ apiService.getAllDevice(null,10,pageNum)},devInfo,true)
    }

}