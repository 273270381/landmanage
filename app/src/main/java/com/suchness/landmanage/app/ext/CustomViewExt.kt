package com.suchness.landmanage.app.ext

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.model.LatLng
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.chad.library.adapter.base.BaseBinderAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.suchness.deeplearningapp.app.weight.recyclerview.DefineLoadMoreView
import com.suchness.landmanage.R
import com.suchness.landmanage.app.utils.DatetimeUtil
import com.suchness.landmanage.app.utils.SettingUtil
import com.suchness.landmanage.data.been.ResultInfo
import com.suchness.landmanage.ui.fragment.*
import com.yanzhenjie.recyclerview.SwipeRecyclerView
import me.hgj.jetpackmvvm.base.Ktx
import me.hgj.jetpackmvvm.demo.app.weight.loadCallBack.EmptyCallback
import me.hgj.jetpackmvvm.demo.app.weight.loadCallBack.ErrorCallback
import me.hgj.jetpackmvvm.demo.app.weight.loadCallBack.LoadingCallback
import me.hgj.jetpackmvvm.ext.util.notNull
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * @author: hejunfeng
 * @date: 2021/9/24 0024
 */
fun Toolbar.init(titleStr: String = ""): Toolbar{
    Ktx.app?.let { SettingUtil.getColor(it) }?.let { setBackgroundColor(it) }
    title = titleStr
    return this
}

/**
 * ???????????????
 */
fun hideSoftKeyboard(activity: Activity?) {
    activity?.let { act ->
        val view = act.currentFocus
        view?.let {
            val inputMethodManager = act.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}

fun ViewPager2.initMain(fragment : Fragment) : ViewPager2{
    //???????????????
    this.isUserInputEnabled = false
    this.offscreenPageLimit = 5
    //???????????????
    adapter = object : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            when(position){
                0 -> {
                    return AlarmFragment()
                }
                1 -> {
                    return BaiduMapFragment()
                }
                2 -> {
                    return MonitorFragment()
                }
                3 -> {
                    return FileFragment()
                }
                else -> {
                    return AlarmFragment()
                }
            }
        }

    }
    return this
}

fun BottomNavigationViewEx.init(navigationItemSelectedAction : (Int) -> Unit): BottomNavigationViewEx {
    enableAnimation(true)
    enableShiftingMode(false)
    enableItemShiftingMode(true)
    itemIconTintList = Ktx.app?.let { SettingUtil.getColor(it) }?.let {
        SettingUtil.getColorStateList(
            it
        )
    }
    itemTextColor = Ktx.app?.let { SettingUtil.getColorStateList(it) }
    setTextSize(12F)
    setOnNavigationItemSelectedListener {
        navigationItemSelectedAction.invoke(it.itemId)
        true
    }
    return this
}

/**
 * ??????BottomNavigation???????????? ?????????????????????Toast
 * @receiver BottomNavigationViewEx
 * @param ids IntArray
 */
fun BottomNavigationViewEx.interceptLongClick(vararg ids:Int){
    val bottomNavigationMenuView: ViewGroup = (this.getChildAt(0) as ViewGroup)
    for (index in ids.indices){
        bottomNavigationMenuView.getChildAt(index).findViewById<View>(ids[index]).setOnLongClickListener {
            true
        }
    }
}

//fun MapView.init(fragment: Fragment ) : BaiduMap {
//    fragment.let {
//        showZoomControls(false)
//        // ?????????????????????Logo
//        removeViewAt(1)
//        //???????????????
//        val mBaiduMap = it.mapView.map
//        mBaiduMap.let {
//            it.isTrafficEnabled = true
//            val settings: UiSettings = it.getUiSettings()
//            settings.isCompassEnabled = false
//            settings.isOverlookingGesturesEnabled = false
//            it.mapType = BaiduMap.MAP_TYPE_NORMAL
//            it.isMyLocationEnabled = true
//        }
//        return mBaiduMap
//    }
//}



fun BaiduMap.MarkerListener(getDamId : (String,LatLng) ->Unit){
    setOnMarkerClickListener (object : BaiduMap.OnMarkerClickListener{
        override fun onMarkerClick(p0: Marker?): Boolean {
            if (p0 != null) {
                getDamId.invoke(p0.title,p0.position)
            }
            return true
        }
    })
}

fun LineChart.init() : LineChart{
    setBackgroundColor(Color.rgb(104, 241, 175));
    description.isEnabled = true
    setTouchEnabled(false)
    isDragEnabled = false
    setScaleEnabled(false)
    setPinchZoom(false)
    setDrawBorders(false)
    setDrawGridBackground(false)
    axisRight.isEnabled = false
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
    xAxis.setGranularity(1f)
    xAxis.setValueFormatter(object : ValueFormatter() {
        val mFormat = SimpleDateFormat("M???d")

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val millis = TimeUnit.HOURS.toMillis(value.toLong())
            var date = mFormat.format(Date(millis))
            return date
        }
    })
    invalidate()
    return this
}



fun LineDataSet.set(chart : LineChart ,option : (LineDataSet) -> Unit) : LineDataSet{
    mode = LineDataSet.Mode.CUBIC_BEZIER
    cubicIntensity = 0.2f
    setDrawCircles(false)
    setDrawValues(true)
    lineWidth = 1.8f
    circleRadius = 4f
    setCircleColor(Color.WHITE)
    highLightColor = Color.rgb(244, 117, 117)
    color = Color.WHITE
    fillColor = Color.WHITE
    fillAlpha = 100
    setDrawHorizontalHighlightIndicator(false)
    setFillFormatter(IFillFormatter { dataSet, dataProvider ->
        chart.getAxisLeft().getAxisMinimum()
    })
    option.invoke(this)
    return this
}

fun LoadService<*>.showLoading(){
    this.showCallback(LoadingCallback::class.java)
}

fun loadServiceInit(view : View , callback: ()->Unit) : LoadService<Any>{
    val loadsir = LoadSir.getDefault().register(view)
    {
        callback.invoke()
    }
    loadsir.showSuccess()
    Ktx.app?.let { SettingUtil.getColor(it) }?.let { SettingUtil.setLoadingColor(it,loadsir) }
    return loadsir
}

fun ViewPager2.init(fragment: Fragment,fragments: ArrayList<Fragment>,isUserInputEnabled: Boolean = true): ViewPager2{
    //???????????????
    this.isUserInputEnabled = isUserInputEnabled
    adapter = object : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
    return this
}


//??????SwipeRecyclerView
fun SwipeRecyclerView.init(
    layoutManger: RecyclerView.LayoutManager,
    bindAdapter: RecyclerView.Adapter<*>,
    isScroll: Boolean = true
): SwipeRecyclerView {
    layoutManager = layoutManger
    setHasFixedSize(true)
    adapter = bindAdapter
    isNestedScrollingEnabled = isScroll
    return this
}


//??????????????????????????????
fun BaseQuickAdapter<*, *>.setAdapterAnimation(mode: Int) {
    //??????0????????????????????? ????????????
    if (mode == 0) {
        this.animationEnable = false
    } else {
        this.animationEnable = true
        this.setAnimationWithDefault(BaseQuickAdapter.AnimationType.values()[mode - 1])
    }
}


fun SwipeRecyclerView.initFooter(loadmoreListener: SwipeRecyclerView.LoadMoreListener): DefineLoadMoreView {
    val footerView = Ktx.app?.let { DefineLoadMoreView(it) }
    //?????????????????????
    Ktx.app?.let { SettingUtil.getOneColorStateList(it) }?.let { footerView?.setLoadViewColor(it) }
    //????????????????????????
    footerView?.setmLoadMoreListener(SwipeRecyclerView.LoadMoreListener {
        footerView.onLoading()
        loadmoreListener.onLoadMore()
    })
    this.run {
        //????????????????????????
        addFooterView(footerView)
        setLoadMoreView(footerView)
        //????????????????????????
        setLoadMoreListener(loadmoreListener)
    }
    return footerView!!
}


fun RecyclerView.initFloatBtn(floatbtn: FloatingActionButton) {
    //??????recyclerview?????????????????????????????????????????????????????????????????????
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        @SuppressLint("RestrictedApi")
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (!canScrollVertically(-1)) {
                floatbtn.visibility = View.INVISIBLE
            }
        }
    })
    floatbtn.backgroundTintList = Ktx.app?.let { SettingUtil.getOneColorStateList(it) }
    floatbtn.setOnClickListener {
        val layoutManager = layoutManager as LinearLayoutManager
        //????????????recyclerview ?????????????????????????????????????????????40????????????????????????????????????????????????????????????????????????
        if (layoutManager.findLastVisibleItemPosition() >= 40) {
            scrollToPosition(0)//?????????????????????????????????(??????)
        } else {
            smoothScrollToPosition(0)//??????????????????????????????(?????????)
        }
    }
}

//????????? SwipeRefreshLayout
fun SwipeRefreshLayout.init(onRefreshListener: () -> Unit) {
    this.run {
        setOnRefreshListener {
            onRefreshListener.invoke()
        }
        //??????????????????
        Ktx.app?.let { SettingUtil.getColor(it) }?.let { setColorSchemeColors(it) }
    }
}


//fun DropDownMenu.initDel(fragment: Fragment, dams : MutableList<DamDtos>, types: MutableList<DeviceType>, swipeRefreshLayout: SwipeRefreshLayout, refresh :(dam : Int, devType : String) -> Unit){
//    var damPosition = 0;
//    var typePosition = 0;
//    val constellationView = fragment.layoutInflater.inflate(R.layout.custom_layout_tel,null)
//    var header = arrayOf(fragment.getString(R.string.filter))
//
//    val damAdapter = ConstellationAdapter(context, dams.stream().map(DamDtos :: name).collect(Collectors.toList()))
//    constellationView.constellation1.adapter = damAdapter
//    val typeAdapter = ConstellationAdapter(context, types.stream().map(DeviceType :: typeName).collect(Collectors.toList()))
//    constellationView.constellation2.adapter = typeAdapter
//
//    val popupViews: MutableList<View> = java.util.ArrayList()
//    popupViews.add(constellationView)
//    val convertview = TextView(context)
//    convertview.setBackgroundResource(R.color.transparent_75)
//    setDropDownMenu(listOf(*header),popupViews, convertview)
//    post {
//        swipeRefreshLayout.scrollY = DataUtils.getObsetData(tabMenuView.measuredHeight)
//    }
//    constellationView.constellation1.setOnItemClickListener { parent, view, position, id ->
//        damAdapter.setCheckItem(position);
//        damPosition = position
//    }
//
//    constellationView.constellation2.setOnItemClickListener { parent, view, position, id ->
//        typeAdapter.setCheckItem(position);
//        typePosition = position
//    }
//    constellationView.cancel.setOnClickListener {
//        closePopuMenu()
//    }
//    constellationView.ok.setOnClickListener {
//        refresh.invoke(dams.get(damPosition).id.toInt(),types.get(typePosition).typeName)
//        closePopuMenu()
//    }
//}


//fun DropDownMenu.init(fragment: Fragment , data: ResultInfo<DamDtos>,swipeRefreshLayout: SwipeRefreshLayout, refresh :(address : Int, startTime: String, endTime: String) -> Unit){
//    var constellationPosition = 0;
//    val constellationView = fragment.layoutInflater.inflate(R.layout.custom_layout,null)
//    var header = arrayOf(fragment.getString(R.string.filter))
//    val dropDownAdapter = ConstellationAdapter(context, data.list.stream().map(DamDtos :: name).collect(Collectors.toList()))
//    constellationView.constellation.adapter = dropDownAdapter
//    val popupViews: MutableList<View> = java.util.ArrayList()
//    popupViews.add(constellationView)
//    val convertview = TextView(context)
//    convertview.setBackgroundResource(R.color.transparent_75)
//    setDropDownMenu(listOf(*header),popupViews, convertview)
//    post {
//        swipeRefreshLayout.scrollY = DataUtils.getObsetData(tabMenuView.measuredHeight)
//    }
//    fragment.context?.let {
//        constellationView.startTime.initCustomTimePicker(it)
//        constellationView.endTime.initCustomTimePicker(it)
//    }
//    constellationView.constellation.setOnItemClickListener { parent, view, position, id ->
//        dropDownAdapter.setCheckItem(position);
//        constellationPosition = position
//    }
//    constellationView.cancel.setOnClickListener {
//        closePopuMenu()
//    }
//    constellationView.ok.setOnClickListener {
//        refresh.invoke(data.list.get(constellationPosition).id.toInt(),startTime.text.toString().trim(),endTime.text.toString().trim())
//        closePopuMenu()
//    }
//}

fun TextView.initCustomTimePicker(context: Context) : TimePickerView{
    /**
     * @description
     *
     * ???????????????
     * 1.?????????????????????id??? optionspicker ?????? timepicker ???????????????????????????????????????????????????????????????.
     * ???????????????demo ????????????????????????layout?????????
     * 2.????????????Calendar???????????????0-11???,?????????????????????Calendar???set?????????????????????,???????????????????????????0-11
     * setRangDate??????????????????????????????(?????????????????????????????????????????????1900-2100???????????????????????????)
     */
    val selectedDate = Calendar.getInstance() //??????????????????
    selectedDate.set(Calendar.HOUR_OF_DAY, 0)
    selectedDate.set(Calendar.MINUTE, 0)
    selectedDate.set(Calendar.SECOND, 0)

    val startDate = Calendar.getInstance()
    startDate[2018, 1] = 23
    val endDate = Calendar.getInstance()
    endDate[2027, 2] = 28
    //??????????????? ??????????????????
    var pvCustomTime : TimePickerView? = null
    pvCustomTime = TimePickerBuilder(context, OnTimeSelectListener { date, v -> //??????????????????
        setText(DatetimeUtil.formatDate(date,"yyyy-MM-dd HH:mm:ss"))
    }) /*.setType(TimePickerView.Type.ALL)//default is all
                .setCancelText("Cancel")
                .setSubmitText("Sure")
                .setContentTextSize(18)
                .setTitleSize(20)
                .setTitleText("Title")
                .setTitleColor(Color.BLACK)
               / *.setDividerColor(Color.WHITE)//????????????????????????
                .setTextColorCenter(Color.LTGRAY)//????????????????????????
                .setLineSpacingMultiplier(1.6f)//????????????????????????????????????
                .setTitleBgColor(Color.DKGRAY)//?????????????????? Night mode
                .setBgColor(Color.BLACK)//?????????????????? Night mode
                .setSubmitColor(Color.WHITE)
                .setCancelColor(Color.WHITE)*/
        /*.animGravity(Gravity.RIGHT)// default is center*/
        .setDate(selectedDate)
        .setRangDate(startDate, endDate)
        .setLayoutRes(R.layout.pickerview_custom_time) { v ->
            val tvSubmit = v.findViewById<View>(R.id.tv_finish) as TextView
            val ivCancel = v.findViewById<View>(R.id.iv_cancel) as ImageView
            tvSubmit.setOnClickListener {
                pvCustomTime?.returnData()
                pvCustomTime?.dismiss()
            }
            ivCancel.setOnClickListener { pvCustomTime?.dismiss() }
        }
        .setContentTextSize(18)
        .setType(booleanArrayOf(true, true, true, true, true, true))
        .setLabel("???", "???", "???", "???", "???", "???")
        .setLineSpacingMultiplier(1.2f)
        .setTextXOffset(0, 0, 0, 40, 0, -40)
        .isCenterLabel(false) //?????????????????????????????????label?????????false?????????item???????????????label???
        .setDividerColor(-0xdb5263)
        .build()
    setOnClickListener {
        pvCustomTime.show()
    }
    return pvCustomTime
}

/**
 * ????????????????????????????????????????????????????????????????????? ????????????????????????????????????????????? Textview???FragmentLayout???????????????????????????
 * ???????????????BottomNavigationViewEx????????????????????????FragmentLayout???????????? is Fragmentlayout????????? is BottomNavigationViewEx??????
 * ??????????????????????????? is FragmentLayout???????????? ?????? is BottomNavigationViewEx???????????????
 */
fun setUiTheme(color: Int, vararg anyList: Any?) {
    anyList.forEach { view ->
        view?.let {
            when (it) {
                is LoadService<*> -> SettingUtil.setLoadingColor(color, it as LoadService<Any>)
                is FloatingActionButton -> it.backgroundTintList =
                    SettingUtil.getOneColorStateList(color)
                is SwipeRefreshLayout -> it.setColorSchemeColors(color)
//                is DefineLoadMoreView -> it.setLoadViewColor(SettingUtil.getOneColorStateList(color))
                is BottomNavigationViewEx -> {
                    it.itemIconTintList = SettingUtil.getColorStateList(color)
                    it.itemTextColor = SettingUtil.getColorStateList(color)
                }
                is Toolbar -> it.setBackgroundColor(color)
                is TextView -> it.setTextColor(color)
                is LinearLayout -> it.setBackgroundColor(color)
                is ConstraintLayout -> it.setBackgroundColor(color)
                is FrameLayout -> it.setBackgroundColor(color)
            }
        }
    }
}

/**
 * ???????????????
 */
fun LoadService<*>.showEmpty() {
    this.showCallback(EmptyCallback::class.java)
}

/**
 * ??????????????????
 * @param message ?????????????????????????????????
 */
fun LoadService<*>.showError(message: String = "") {
    this.setErrorText(message)
    this.showCallback(ErrorCallback::class.java)
}


fun LoadService<*>.setErrorText(message: String) {
    if (message.isNotEmpty()) {
        this.setCallBack(ErrorCallback::class.java) { _, view ->
            view.findViewById<TextView>(R.id.error_text).text = message
        }
    }
}


/**
 * ??????????????????
 */
fun <T> loadListData(
    data: ResultInfo<T>,
    baseQuickAdapter: BaseQuickAdapter<T, *>?,
    loadService: LoadService<*>,
    recyclerView: SwipeRecyclerView,
    swipeRefreshLayout: SwipeRefreshLayout
) {
    swipeRefreshLayout.isRefreshing = false
    recyclerView.loadMoreFinish(data?.list.isEmpty(), data?.totalPage > data?.pageNum)
    data.notNull({
        //??????
        when {
            //???????????????????????? ?????????????????????
            data?.list.isEmpty() -> {
                loadService.showEmpty()
            }
            //????????????
            data?.pageNum == 1 -> {
                baseQuickAdapter?.setList(data?.list)
                loadService.showSuccess()
            }
            //???????????????
            else -> {
                baseQuickAdapter?.addData(data?.list)
                loadService.showSuccess()
            }
        }
    },{
        loadService.showError("???????????????")
    })
}

fun loadListData2(
    data: ResultInfo<out Any>,
    baseQuickAdapter: BaseBinderAdapter?,
    loadService: LoadService<*>,
    recyclerView: SwipeRecyclerView,
    swipeRefreshLayout: SwipeRefreshLayout
) {
    swipeRefreshLayout.isRefreshing = false
    recyclerView.loadMoreFinish(data?.list.isEmpty(), data?.totalPage > data?.pageNum)
    data.notNull({
        //??????
        when {
            //???????????????????????? ?????????????????????
            data?.list.isEmpty() -> {
                loadService.showEmpty()
            }
            //????????????
            data?.pageNum == 1 -> {
                baseQuickAdapter?.setList(data?.list)
                loadService.showSuccess()
            }
            //???????????????
            else -> {
                baseQuickAdapter?.addData(data?.list)
                loadService.showSuccess()
            }
        }
    },{
        loadService.showError("???????????????")
    })
}