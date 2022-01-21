package com.suchness.landmanage.viewmodel

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.esri.core.geometry.Point
import com.suchness.deeplearningapp.app.network.ApiService
import com.suchness.deeplearningapp.app.network.apiService
import com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_TIF_PATH
import com.suchness.landmanage.app.utils.ReadKml
import com.suchness.landmanage.data.been.ResultInfo
import com.suchness.landmanage.data.been.StyleId
import com.suchness.landmanage.data.been.StyleMap
import com.suchness.landmanage.data.been.fileInfo
import kotlinx.coroutines.launch
import me.hgj.jetpackmvvm.base.viewmodel.BaseViewModel
import me.hgj.jetpackmvvm.callback.databind.StringObservableField
import me.hgj.jetpackmvvm.ext.download.DownLoadManager
import me.hgj.jetpackmvvm.ext.download.DownloadResultState
import me.hgj.jetpackmvvm.ext.download.downLoadExt
import me.hgj.jetpackmvvm.ext.request
import me.hgj.jetpackmvvm.state.ResultState
import java.util.*
import me.hgj.jetpackmvvm.callback.livedata.event.EventLiveData as EventLiveData

/**
 * @author: hejunfeng
 * @date: 2021/12/14 0014
 */
class MapViewModel: BaseViewModel() {
    var path = StringObservableField(DEFAULT_SAVE_TIF_PATH + "south")

    var KmlName = MutableLiveData<String>("南区.kml")

    var files = MutableLiveData<ResultState<MutableList<fileInfo>>>()

    var downloadData: MutableLiveData<DownloadResultState> = MutableLiveData()

    var cameraInfo = MutableLiveData<MutableMap<String , List<Any?>>>()

    var info = MutableLiveData<MutableMap<String , List<Any?>>>()

    fun requestFile(fileName: String){
        request({ apiService.getPicList(fileName,null,null,null,null,null,null)},files,false)
    }

    fun downLoadFile(path: String,fileName: String,time: String){
        viewModelScope.launch {
            var tag = fileName
            var url = ApiService.SERVER_URL+"minio/download/"+fileName+"/"+time

            DownLoadManager.downLoad(tag,url,path,fileName,false, downLoadExt(downloadData))
        }

    }

    fun loadCamera(ctx: Context?, url: String?) {
        val readKml = ReadKml(url, ctx)
        readKml.parseKml2()
        val list_name: List<String?> = readKml.getList_name()
        val list_des: List<String?> = readKml.getList_des()
        val list_point: List<Point?> = readKml.getList_point()
        val data: MutableMap<String, List<Any?>> = LinkedHashMap()
        data["name"] = list_name
        data["des"] = list_des
        data["point"] = list_point
        cameraInfo.setValue(data)
    }


    fun loadInfo(ctx: Context?) {
        val readKml = ReadKml(KmlName.value, ctx)
        readKml.parseKml()
        val list_name_info = readKml.list_name
        val list_des_info = readKml.list_des
        val list_collection = readKml.list_collection
        val list_styleid: List<StyleId?> = readKml.list_styleid
        val list_stylemap: List<StyleMap?> = readKml.list_stylemap
        val list_style_url = readKml.list_style_url
        val data: MutableMap<String, List<Any?>> = LinkedHashMap()
        data["name"] = list_name_info
        data["des"] = list_des_info
        data["collection"] = list_collection
        data["styleId"] = list_styleid
        data["styleMap"] = list_stylemap
        data["styleUrl"] = list_style_url
        info.setValue(data)
    }


    fun setFiles(cid : String){
        when(cid){
            "西区"-> {
                path.set(DEFAULT_SAVE_TIF_PATH + "west")
                KmlName.value = "西区.kml"
            }
            "南区"-> {
                path.set(DEFAULT_SAVE_TIF_PATH + "south")
                KmlName.value = "南区.kml"
            }
        }
    }
}
