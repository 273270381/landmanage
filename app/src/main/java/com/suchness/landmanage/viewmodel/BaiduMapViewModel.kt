package com.suchness.landmanage.viewmodel

import androidx.lifecycle.MutableLiveData
import com.baidu.mapapi.map.BaiduMap
import com.suchness.deeplearningapp.app.network.apiService
import com.suchness.landmanage.data.been.deviceInfo
import me.hgj.jetpackmvvm.base.viewmodel.BaseViewModel
import me.hgj.jetpackmvvm.ext.request
import me.hgj.jetpackmvvm.state.ResultState

/**
 * @author: hejunfeng
 * @date: 2021/12/28 0028
 */
class BaiduMapViewModel : BaseViewModel() {

    var devices = MutableLiveData<ResultState<MutableList<deviceInfo>>>()

    fun requestFile(){
        request({ apiService.getAllDeviceInfo(null)},devices,true)
    }
}

