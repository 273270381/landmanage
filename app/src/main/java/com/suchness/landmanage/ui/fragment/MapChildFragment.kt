package com.suchness.landmanage.ui.fragment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.ToastUtils
import com.esri.android.map.FeatureLayer
import com.esri.android.map.GraphicsLayer
import com.esri.android.map.LocationDisplayManager
import com.esri.android.map.MapView
import com.esri.android.map.event.OnSingleTapListener
import com.esri.android.map.event.OnZoomListener
import com.esri.android.runtime.ArcGISRuntime
import com.esri.core.geodatabase.ShapefileFeatureTable
import com.esri.core.geometry.*
import com.esri.core.map.Graphic
import com.esri.core.renderer.SimpleRenderer
import com.esri.core.symbol.*
import com.kingja.loadsir.core.LoadService
import com.suchness.deeplearningapp.app.base.BaseFragment
import com.suchness.deeplearningapp.ui.activity.MainActivity
import com.suchness.landmanage.R
import com.suchness.landmanage.app.ext.loadServiceInit
import com.suchness.landmanage.app.ext.showLoading
import com.suchness.landmanage.app.ext.showMessage
import com.suchness.landmanage.app.network.DownLoadManager
import com.suchness.landmanage.app.network.download.ProgressCallBack
import com.suchness.landmanage.app.utils.*
import com.suchness.landmanage.data.been.StyleId
import com.suchness.landmanage.data.been.StyleMap
import com.suchness.landmanage.databinding.FragmentMapBinding
import com.suchness.landmanage.viewmodel.MapViewModel
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.include_recyclerview.*
import me.hgj.jetpackmvvm.ext.download.DownloadResultState
import me.hgj.jetpackmvvm.ext.parseState
import okhttp3.ResponseBody
import java.io.File
import java.io.FileNotFoundException
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.stream.Collectors

/**
 * @author: hejunfeng
 * @date: 2021/12/14 0014
 */
class MapChildFragment: BaseFragment<MapViewModel, FragmentMapBinding>(),OnSingleTapListener,OnZoomListener{
    private var locationDisplayManager: LocationDisplayManager?= null
    private var graphicsLayer : GraphicsLayer = GraphicsLayer()
    private var graphicsLayer_camera : GraphicsLayer = GraphicsLayer()
    private var graphicsLayer_info : GraphicsLayer = GraphicsLayer()
    private var graphicsLayer_info_text : GraphicsLayer = GraphicsLayer()
    private var fileNameList: List<String> = ArrayList()
    private var hasMap = false
    private var handler : Handler? = null;
    private var handlerKml : Handler? = null;
    var TaskLatch = CountDownLatch(1)

    private var progress = 0
    private var length = 0;
    private var mapview :MapView ?= null
    override fun layoutId(): Int {
        return R.layout.fragment_map
    }
    private var cid = ""
    private val pointList: MutableList<Point> = ArrayList()
    private lateinit var loadsir: LoadService<Any>


    @RequiresApi(Build.VERSION_CODES.N)
    override fun initView(savedInstanceState: Bundle?) {
        loadsir = loadServiceInit(mDatabind.root){
            //点击重试时触发的操作
            loadsir.showLoading()
            showLoading("正在下载，请稍后...")
        }
        loadsir.showLoading()
        mDatabind.click =MapClick()
        arguments?.let {
            cid = it.getString("cid","")
        }
        mViewModel.setFiles(cid)
        mapview = mDatabind.mapView
        //设置授权
        ArcGISRuntime.setClientId("Gxw2gDOFkkdudimV")
        mapview!!.setEsriLogoVisible(false)
        mapview!!.onSingleTapListener = this
        mapview!!.onSingleTapListener = this
        if (locationDisplayManager == null) locationDisplayManager= mapview?.locationDisplayManager
        try {
            locationDisplayManager.let {
                it?.isAllowNetworkLocation=true
                it?.isAccuracyCircleOn=false
                it?.isShowLocation=true
                it?.isShowPings=true
                it?.setDefaultSymbol(PictureMarkerSymbol(BitmapDrawable(BitmapFactory.decodeResource(resources,R.drawable.location))))
                it?.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION)
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
        handler = Handler()
        handler!!.postDelayed(Runnable {
            loadlayer(mViewModel.path.get());
        },1000)
        var mNotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel("id", "name", NotificationManager.IMPORTANCE_LOW)
            mNotificationManager.createNotificationChannel(mChannel)
        }
        graphicsLayer_info_text.isVisible = false
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadlayer(path: String){
        val file = File(path)
        val basePath = path.substring(path.lastIndexOf("/") + 1) //west


        if (!file.exists()){
            maplayout.visibility = GONE
            progressLayout.visibility = VISIBLE
            SweetAlertDialogUtils.showBasicDialog(context,"下载","是否下载"+cid+"影像?",object : SweetAlertDialogUtils.DialogCallBack{
                override fun conform() {
                    mViewModel.requestFile(basePath)
                }

                override fun cancel() {
                }

            })
        }else{
            maplayout.visibility = GONE
            progressLayout.visibility = VISIBLE
            var files = emptyArray<File>()
            files = file.listFiles()
            if (files.size < CacheUtil.getFileNum()) {
                fileNameList = Arrays.stream(files).map { s: File -> s.name }
                    .collect(Collectors.toList())
                SweetAlertDialogUtils.showBasicDialog(
                    context,
                    "下载",
                    "是否下载"+cid+"影像?",
                    object : SweetAlertDialogUtils.DialogCallBack{
                        override fun conform() {
                            mViewModel.requestFile(basePath)
                        }

                        override fun cancel() {
                            TODO("Not yet implemented")
                        }
                    })
            }else{
                maplayout.visibility = VISIBLE
                progressLayout.visibility = GONE
                hasMap = true
                //加载shp
                try {
                    val shapefileFeatureTable = ShapefileFeatureTable("$path/polyline$basePath.shp")
                    val featureLayer = FeatureLayer(shapefileFeatureTable)
                    featureLayer.renderer = SimpleRenderer(SimpleFillSymbol(Color.RED))
                    mapview?.addLayer(featureLayer)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                //加载tif
                for (i in 0 until CacheUtil.getTifNum()) {
                    AppOperator.runOnMainThread(ImageTask(TaskLatch, path, i, mapview, basePath))
                }
                //加载kml
                handlerKml = Handler()
                handlerKml!!.postDelayed(Runnable {
                    mViewModel.loadCamera(context, "camera.kml")
                    mViewModel.loadInfo(context)
                },1000)
                loadsir.showSuccess()
            }
        }
    }

    override fun lazyLoadData() {
        super.lazyLoadData()
    }

    override fun createObserver() {
        mViewModel.files.observe(viewLifecycleOwner, androidx.lifecycle.Observer { it ->
            parseState(it,{
                downloadProgressBar.progress = progress
                downloadProgress.text = "$progress%"
                length = it.size
                it.forEach { item ->
                    if (!fileNameList.contains(item.fileName)){
                        getDownTif(mViewModel.path.get(), item.fileName, item.time)
                    }else{
                        progress++
                    }
                }
            })
        })

//        mViewModel.downloadData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
//            when(it){
//                is DownloadResultState.Success -> {
//                    //下载成功
//                    downloadProgressBar.progress = progress
//                    downloadProgress.text = "$progress%"
//                    progress++;
//                    if (progress == length){
//                        downloadProgressBar.progress = 100
//                        downloadProgress.text = "100%"
//                        //todo
//                    }
//                }
//
//                is DownloadResultState.Progress -> {
//                    //下载中
//
//                }
//
//                is DownloadResultState.Error -> {
//                    Log.d("hjf","文件下载失败！:"+ it.errorMsg)
//                }
//            }
//        })

        mViewModel.cameraInfo.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val list_name = it.get("name") as List<String>
            val list_des = it.get("des") as List<String>
            val list_point = it.get("point") as MutableList<Point>
            val pictureMarkerSymbol = PictureMarkerSymbol(context?.getResources()?.getDrawable(R.drawable.point) as BitmapDrawable)
            for (i in list_point.indices) {
                val map: MutableMap<String, Any> = HashMap()
                map["style"] = "marker"
                map["name"] = list_name[i + 2]
                map["des"] = list_des[i]
                val pointGraphic = Graphic(list_point[i], pictureMarkerSymbol, map)
                val t = TextSymbol(12, list_name[i + 2], Color.GREEN)
                t.fontFamily = File(CopyFontFile.FONT_PATH).getPath()
                t.offsetX = -10f
                t.offsetY = -22f
                val map2: MutableMap<String, Any> = HashMap()
                map2["style"] = "text"
                val graphic_text = Graphic(list_point[i], t, map2)
                graphicsLayer_camera.addGraphic(pointGraphic)
                graphicsLayer_camera.addGraphic(graphic_text)
                mapview?.addLayer(graphicsLayer_camera)
            }
        })

        mViewModel.info.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val list_name_info = it.get("name") as List<String>
            val list_des_info = it.get("des") as List<String>
            val list_collection = it.get("collection") as List<List<Point>>
            val list_styleid: List<StyleId> = it.get("styleId") as List<StyleId>
            val list_stylemap: List<StyleMap> = it.get("styleMap") as List<StyleMap>
            val list_style_url = it.get("styleUrl") as List<String>
            //线型
            for (i in list_collection.indices) {
                val polyline = Polyline()
                polyline.startPath(list_collection[i][0])
                for (j in 1 until list_collection[i].size) {
                    polyline.lineTo(list_collection[i][j])
                }
                val map: MutableMap<String, Any> = HashMap()
                map["style"] = "line"
                val url = list_style_url[i]
                var linecolor = ""
                var linewidth = ""
                for (styleMap in list_stylemap) {
                    if (styleMap.id.equals(url)) {
                        val stylemapUrl = styleMap.styleUrl
                        for (styleid in list_styleid) {
                            if (styleid.id.equals(stylemapUrl)) {
                                linecolor = styleid.lineColor
                                linewidth = styleid.lineWidth
                            }
                        }
                    }
                }
                val simpleLineSymbol_info = SimpleLineSymbol(
                    Color.parseColor(
                        "#$linecolor"
                    ),
                    linewidth.toInt().toFloat()
                )
                val line = Graphic(polyline, simpleLineSymbol_info, map)
                val t = TextSymbol(12, list_name_info[i + 1] + list_des_info[i], Color.WHITE)
                t.fontFamily = File(CopyFontFile.FONT_PATH).path
                t.offsetX = -20f
                val map2: MutableMap<String, Any> = HashMap()
                map2["style"] = "text"
                val ts = Graphic(polyline, t, map2)
                graphicsLayer_info.addGraphic(line)
                graphicsLayer_info_text.addGraphic(ts)
                mapview?.addLayer(graphicsLayer_info)
                mapview?.addLayer(graphicsLayer_info_text)
                mapview?.addLayer(graphicsLayer)
            }

        })
        super.createObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (handler != null){
            handler!!.removeCallbacksAndMessages(null)
        }
        if (handlerKml != null){
            handlerKml!!.removeCallbacksAndMessages(null)
        }
        if (locationDisplayManager!!.isStarted) {
            locationDisplayManager!!.stop()
        }
        mapview?.recycle()
    }


//    private fun getDownTif(path: String,fileName: String, time: String){
//        mViewModel.downLoadFile(path,fileName,time)
//    }

    private fun getDownTif(path: String, fileName: String, time: String) {
        DownLoadManager.getInstance().load(fileName, time, object : ProgressCallBack<ResponseBody?>(path, fileName) {

                override fun onSuccess(responseBody: ResponseBody?) {
                    //下载成功
                    downloadProgressBar.progress = progress++;
                    downloadProgress.text = "$progress%"
                    if (progress == length){
                        downloadProgressBar.progress = 100
                        downloadProgress.text = "100%"
                        downloadtext.text="下载完成!"
                        //todo
                        activity?.recreate()
                    }
                }

                override fun progress(progress: Long, total: Long) {}

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    downloadProgressBar.progress = progress++;
                    downloadProgress.text = "$progress%"
                    if (progress == length){
                        downloadProgressBar.progress = 100
                        downloadProgress.text = "100%"
                        downloadtext.text="下载完成!"
                        //todo
                        activity?.recreate()
                    }
                }
                override fun onCompleted() {}
            })
    }

    override fun onSingleTap(v: Float, v1: Float) {
        if (hasMap){
            if (result.getVisibility() == View.VISIBLE){
                var p : Point = mapView.toMapPoint(v, v1);
                pointList.add(p);
                //点，线，面样式
                var simpleMarkerSymbol: SimpleMarkerSymbol = SimpleMarkerSymbol(Color.BLACK, 6, SimpleMarkerSymbol.STYLE.CIRCLE);
                var simpleLineSymbol: SimpleLineSymbol = SimpleLineSymbol(Color.BLACK, 2F);
                var simpleFillSymbol: SimpleFillSymbol = SimpleFillSymbol(Color.YELLOW);
                simpleFillSymbol.setAlpha(90);
                simpleFillSymbol.setOutline(SimpleLineSymbol(Color.argb(0, 0, 0, 0), 1F));
                if (pointList.size == 1) {
                    var point: Graphic = Graphic(p, simpleMarkerSymbol);
                    graphicsLayer.addGraphic(point);
                }else if (pointList.size == 2) {
                    graphicsLayer.removeAll();
                    var polyline: Polyline = Polyline();
                    polyline.startPath(pointList.get(0));
                    polyline.lineTo(p);
                    var line : Graphic =  Graphic(polyline, simpleLineSymbol);
                    graphicsLayer.addGraphic(line);
                    var distance : Double = GeometryEngine.geodesicDistance(pointList.get(0), pointList.get(1), mapView.getSpatialReference(),  LinearUnit(LinearUnit.Code.METER));
                    var distance_2: Double = BigDecimal(distance).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
                    result.setText("距离为:" + distance_2 + "米");
                }else if (pointList.size > 2) {
                    graphicsLayer.removeAll();
                    var polygon : Polygon = Polygon();
                    polygon.startPath(pointList.get(0));
                    for (point in pointList){
                        polygon.lineTo(point);
                    }
                    var gon : Graphic =  Graphic(polygon, simpleFillSymbol);
                    graphicsLayer.addGraphic(gon);
                    var area : Double = GeometryEngine.geodesicArea(polygon, mapView.getSpatialReference(),  AreaUnit(AreaUnit.Code.SQUARE_METER));
                    var area_2: Double =  BigDecimal(area).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
                    var mu: Double = area * 0.0015;
                    var b: BigDecimal =  BigDecimal(mu);
                    //保留小数点后两位
                    var mu_2 : Double = b.setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
                    result.setText("面积为:" + area_2 + "平方米/" + mu_2 + "亩");
                }
            }else{
//                var objectIds : Int[] = graphicsLayer_camera.getGraphicIDs(v, v1, 20);
//                if (objectIds != null && objectIds.length > 0) {
//                    for (int i = 0; i < objectIds.length; i++) {
//                        Graphic graphic = graphicsLayer_camera.getGraphic(objectIds[i]);
//                        if (graphic.getAttributes().get("style").equals("marker")) {
//                            //showDialog(graphic);
//                        }
//                    }
//                }
            }
        }
    }

    override fun preAction(p0: Float, p1: Float, p2: Double) {

    }

    override fun postAction(p0: Float, p1: Float, p2: Double) {
        if (hasMap) {
            //定义地图默认缩放处理后的操作。
            if (mapView.scale > 30000) {
                graphicsLayer_info_text.isVisible = false
            } else {
                graphicsLayer_info_text.isVisible = true
            }
        }
    }

    inner class MapClick {

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun measurement () {
            if (hasMap) {
                if (result.getVisibility() === View.GONE) {
                    result.setVisibility(View.VISIBLE)
                    title_tv.setVisibility(View.GONE)
                    measure_ibtn.setBackground(context?.getDrawable(R.drawable.liangsuan_sel))
                } else {
                    result.setVisibility(View.GONE)
                    title_tv.setVisibility(View.VISIBLE)
                    result.setText("")
                    measure_ibtn.setBackground(context?.getDrawable(R.drawable.liangsuan))
                }
                pointList.clear()
                graphicsLayer.removeAll()
            }
        }

        fun showInfo(){
            if (hasMap) {
                if (graphicsLayer_info.isVisible) {
                    graphicsLayer_info.isVisible = false
                    if (mapView.scale < 30000) {
                        graphicsLayer_info_text.isVisible = false
                    }
                } else {
                    graphicsLayer_info.isVisible = true
                    if (mapView.scale < 30000) {
                        graphicsLayer_info_text.isVisible = true
                    }
                }
            }
        }

        fun zoonIn() {
            if (hasMap) {
                mapView.zoomin()
            }
        }

        fun zoonOut() {
            if (hasMap) {
                mapView.zoomout()
            }
        }


        fun location(){
            if (hasMap) {
                if (!locationDisplayManager!!.isStarted) {
                    locationDisplayManager!!.start()
                    AppOperator.runOnMainThreadDelayed({
                        try {
                            val la = locationDisplayManager!!.location.latitude
                            val ln = locationDisplayManager!!.location.longitude
                            val p = Point(ln, la)
                            val e = mapView.maxExtent
                            if (!e.contains(p)) {
                                locationDisplayManager!!.stop()
                                showMessage("超出地图范围！")
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }, 1500)
                } else {
                    locationDisplayManager!!.stop()
                }
            }
        }

    }

    companion object{
        fun newInstance(cid: String): MapChildFragment{
            val args = Bundle()
            args.putString("cid", cid)
            var fragment = MapChildFragment()
            fragment.arguments = args
            return fragment
        }
    }
}