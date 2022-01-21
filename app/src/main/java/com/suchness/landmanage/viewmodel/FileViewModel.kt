package com.suchness.landmanage.viewmodel

import androidx.lifecycle.MutableLiveData
import com.suchness.deeplearningapp.app.network.apiService
import com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_IMAGE_PATH
import com.suchness.landmanage.app.utils.DataUtils
import com.suchness.landmanage.data.been.ResultInfo
import com.suchness.landmanage.data.been.deviceInfo
import me.hgj.jetpackmvvm.base.viewmodel.BaseViewModel
import me.hgj.jetpackmvvm.ext.request
import me.hgj.jetpackmvvm.state.ResultState

/**
 * @author: hejunfeng
 * @date: 2021/12/25 0025
 */
class FileViewModel: BaseViewModel() {

    var deviceList = MutableLiveData<ResultState<ResultInfo<deviceInfo>>>()

    var FileList = MutableLiveData<String>()

    var pageNum = 1

    fun getAllDevice(){
        request({ apiService.getAllDevice(null,20,pageNum)},deviceList,true)

    }
}