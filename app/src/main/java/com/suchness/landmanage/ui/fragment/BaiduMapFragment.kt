package com.suchness.landmanage.ui.fragment

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.baidu.mapapi.utils.CoordinateConverter
import com.suchness.deeplearningapp.app.base.BaseFragment
import com.suchness.landmanage.R
import com.suchness.landmanage.app.ext.MarkerListener
import com.suchness.landmanage.app.ext.init
import com.suchness.landmanage.app.utils.AppOperator
import com.suchness.landmanage.app.utils.SweetAlertDialogUtils
import com.suchness.landmanage.databinding.FragmentBaidumapBinding
import com.suchness.landmanage.viewmodel.BaiduMapViewModel
import com.videogo.constant.IntentConsts
import com.videogo.openapi.EZOpenSDK
import com.videogo.openapi.bean.EZCameraInfo
import com.videogo.openapi.bean.EZDeviceInfo
import kotlinx.android.synthetic.main.fragment_baidumap.*
import me.hgj.jetpackmvvm.ext.nav
import me.hgj.jetpackmvvm.ext.navigateAction
import me.hgj.jetpackmvvm.ext.parseState
import java.util.stream.Collectors


/**
 * @author: hejunfeng
 * @date: 2021/12/28 0028
 */
class BaiduMapFragment : BaseFragment<BaiduMapViewModel, FragmentBaidumapBinding>() {
    var cameraList : MutableList<EZCameraInfo> = arrayListOf()
    var devList : MutableList<EZDeviceInfo> = arrayListOf()
    var textOverLay :MutableList<Overlay> = arrayListOf()
    var baiduMap : BaiduMap? = null;

    override fun layoutId(): Int {
        return R.layout.fragment_baidumap
    }

    override fun initView(savedInstanceState: Bundle?) {
        AppOperator.runOnThread(Runnable {
            try {
                devList = EZOpenSDK.getInstance().getDeviceList(0,20)
                for (dev in devList!!){
                    cameraList = dev.cameraInfoList
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        })
        baiduMap = map_view.init(this).apply {
            setOnMapStatusChangeListener(object :BaiduMap.OnMapStatusChangeListener{
                override fun onMapStatusChangeStart(p0: MapStatus?) {
                }

                override fun onMapStatusChangeStart(p0: MapStatus?, p1: Int) {
                }

                override fun onMapStatusChange(p0: MapStatus?) {
                }

                override fun onMapStatusChangeFinish(p0: MapStatus?) {
                    AppOperator.runOnThread(Runnable {
                        if (textOverLay.size > 0){
                            if (p0?.zoom!! > 16){
                                for (item in textOverLay){
                                    item.isVisible = true
                                }
                            }else{
                                for (item in textOverLay){
                                    item.isVisible = false
                                }
                            }
                        }
                    })
                }
            })
        }

        baiduMap!!.setOnMapLoadedCallback {
            mViewModel.requestFile()
        }

        baiduMap?.MarkerListener(){ title , latlng ->
            var options :MutableList<OverlayOptions> = arrayListOf()
            var option = MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromResource(R.drawable.point_blue))
            options.add(option)
//                    var overlays = baiduMap?.addOverlays(options)
            var overlay = baiduMap?.addOverlay(option)
            SweetAlertDialogUtils.showBasicDialog(context,title,"查看实时视频？",object : SweetAlertDialogUtils.DialogCallBack{
                @RequiresApi(Build.VERSION_CODES.N)
                override fun conform() {
                    //播放
                    overlay?.remove()
                    nav().navigateAction(R.id.action_to_realPlayFragment,Bundle().apply {
                        cameraList.let {  devList->
                            var cameraInfos = devList.stream().filter { it.cameraName == title}.collect(
                                Collectors.toList())
                            if (cameraInfos.size > 0){
                                putParcelable(IntentConsts.EXTRA_CAMERA_INFO,cameraInfos[0])
                            }
                        }
                        putParcelable(IntentConsts.EXTRA_DEVICE_INFO,devList[0])
                    })
                }

                override fun cancel() {
                    overlay?.remove()
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        map_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        map_view.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        map_view.onDestroy()
    }

    override fun createObserver() {
        mViewModel.devices.observe(viewLifecycleOwner, Observer {
            parseState(it,{ resultInfo ->
                var options : MutableList<OverlayOptions>  = arrayListOf();
                var textOption: MutableList<OverlayOptions>  = ArrayList<OverlayOptions>();
                val builder : LatLngBounds.Builder = LatLngBounds.Builder()
                resultInfo.let {
                    for (dto in resultInfo){
                        if (!dto?.lat.isNullOrEmpty() && !dto?.lng.isNullOrEmpty()){
                            var gps = dto?.lat?.toDouble()?.let { it1 -> dto?.lng?.toDouble()?.let { it2 ->
                                LatLng(it1,
                                    it2
                                )
                            } }
                            val converter = CoordinateConverter()
                                .from(CoordinateConverter.CoordType.GPS)
                                .coord(gps)
                            var latlng = converter.convert();
                            builder.include(latlng)
                            options.add(
                                MarkerOptions().position(latlng).icon(
                                    BitmapDescriptorFactory.fromResource(R.drawable.point)).animateType(
                                    MarkerOptions.MarkerAnimateType.jump).clickable(true).title(dto.devDesc))
                            textOption.add(TextOptions().text(dto.devDesc).fontSize(20).fontColor(R.color.Cyan).position(latlng))
                        }
                    }
                }
                if (builder.build().center != null){
                    val mapStatusUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build())
                    baiduMap?.setMapStatus(mapStatusUpdate);
                    val msu = MapStatusUpdateFactory.zoomBy(-0.8f)
                    baiduMap?.setMapStatus(msu)
                    baiduMap?.addOverlays(options)
                    textOverLay = baiduMap?.addOverlays(textOption)!!
                    for (item in textOverLay){
                        item.isVisible = false
                    }
                }
            })
        })
        super.createObserver()
    }
}