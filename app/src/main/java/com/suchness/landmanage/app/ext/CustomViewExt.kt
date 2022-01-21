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
 * 隐藏软键盘
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
    //是否可滑动
    this.isUserInputEnabled = false
    this.offscreenPageLimit = 5
    //设置适配器
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
 * 拦截BottomNavigation长按事件 防止长按时出现Toast
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
//        // 不显示百度地图Logo
//        removeViewAt(1)
//        //初始化位置
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
        val mFormat = SimpleDateFormat("M月d")

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
    //是否可滑动
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


//绑定SwipeRecyclerView
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


//设置适配器的列表动画
fun BaseQuickAdapter<*, *>.setAdapterAnimation(mode: Int) {
    //等于0，关闭列表动画 否则开启
    if (mode == 0) {
        this.animationEnable = false
    } else {
        this.animationEnable = true
        this.setAnimationWithDefault(BaseQuickAdapter.AnimationType.values()[mode - 1])
    }
}


fun SwipeRecyclerView.initFooter(loadmoreListener: SwipeRecyclerView.LoadMoreListener): DefineLoadMoreView {
    val footerView = Ktx.app?.let { DefineLoadMoreView(it) }
    //给尾部设置颜色
    Ktx.app?.let { SettingUtil.getOneColorStateList(it) }?.let { footerView?.setLoadViewColor(it) }
    //设置尾部点击回调
    footerView?.setmLoadMoreListener(SwipeRecyclerView.LoadMoreListener {
        footerView.onLoading()
        loadmoreListener.onLoadMore()
    })
    this.run {
        //添加加载更多尾部
        addFooterView(footerView)
        setLoadMoreView(footerView)
        //设置加载更多回调
        setLoadMoreListener(loadmoreListener)
    }
    return footerView!!
}


fun RecyclerView.initFloatBtn(floatbtn: FloatingActionButton) {
    //监听recyclerview滑动到顶部的时候，需要把向上返回顶部的按钮隐藏
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
        //如果当前recyclerview 最后一个视图位置的索引大于等于40，则迅速返回顶部，否则带有滚动动画效果返回到顶部
        if (layoutManager.findLastVisibleItemPosition() >= 40) {
            scrollToPosition(0)//没有动画迅速返回到顶部(马上)
        } else {
            smoothScrollToPosition(0)//有滚动动画返回到顶部(有点慢)
        }
    }
}

//初始化 SwipeRefreshLayout
fun SwipeRefreshLayout.init(onRefreshListener: () -> Unit) {
    this.run {
        setOnRefreshListener {
            onRefreshListener.invoke()
        }
        //设置主题颜色
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
     * 注意事项：
     * 1.自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针.
     * 具体可参考demo 里面的两个自定义layout布局。
     * 2.因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
     * setRangDate方法控制起始终止时间(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
     */
    val selectedDate = Calendar.getInstance() //系统当前时间
    selectedDate.set(Calendar.HOUR_OF_DAY, 0)
    selectedDate.set(Calendar.MINUTE, 0)
    selectedDate.set(Calendar.SECOND, 0)

    val startDate = Calendar.getInstance()
    startDate[2018, 1] = 23
    val endDate = Calendar.getInstance()
    endDate[2027, 2] = 28
    //时间选择器 ，自定义布局
    var pvCustomTime : TimePickerView? = null
    pvCustomTime = TimePickerBuilder(context, OnTimeSelectListener { date, v -> //选中事件回调
        setText(DatetimeUtil.formatDate(date,"yyyy-MM-dd HH:mm:ss"))
    }) /*.setType(TimePickerView.Type.ALL)//default is all
                .setCancelText("Cancel")
                .setSubmitText("Sure")
                .setContentTextSize(18)
                .setTitleSize(20)
                .setTitleText("Title")
                .setTitleColor(Color.BLACK)
               / *.setDividerColor(Color.WHITE)//设置分割线的颜色
                .setTextColorCenter(Color.LTGRAY)//设置选中项的颜色
                .setLineSpacingMultiplier(1.6f)//设置两横线之间的间隔倍数
                .setTitleBgColor(Color.DKGRAY)//标题背景颜色 Night mode
                .setBgColor(Color.BLACK)//滚轮背景颜色 Night mode
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
        .setLabel("年", "月", "日", "时", "分", "秒")
        .setLineSpacingMultiplier(1.2f)
        .setTextXOffset(0, 0, 0, 40, 0, -40)
        .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
        .setDividerColor(-0xdb5263)
        .build()
    setOnClickListener {
        pvCustomTime.show()
    }
    return pvCustomTime
}

/**
 * 根据控件的类型设置主题，注意，控件具有优先级， 基本类型的控件建议放到最后，像 Textview，FragmentLayout，不然会出现问题，
 * 列如下面的BottomNavigationViewEx他的顶级父控件为FragmentLayout，如果先 is Fragmentlayout判断在 is BottomNavigationViewEx上面
 * 那么就会直接去执行 is FragmentLayout的代码块 跳过 is BottomNavigationViewEx的代码块了
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
 * 设置空布局
 */
fun LoadService<*>.showEmpty() {
    this.showCallback(EmptyCallback::class.java)
}

/**
 * 设置错误布局
 * @param message 错误布局显示的提示内容
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
 * 加载列表数据
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
        //成功
        when {
            //第一页并没有数据 显示空布局界面
            data?.list.isEmpty() -> {
                loadService.showEmpty()
            }
            //是第一页
            data?.pageNum == 1 -> {
                baseQuickAdapter?.setList(data?.list)
                loadService.showSuccess()
            }
            //不是第一页
            else -> {
                baseQuickAdapter?.addData(data?.list)
                loadService.showSuccess()
            }
        }
    },{
        loadService.showError("网络错误！")
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
        //成功
        when {
            //第一页并没有数据 显示空布局界面
            data?.list.isEmpty() -> {
                loadService.showEmpty()
            }
            //是第一页
            data?.pageNum == 1 -> {
                baseQuickAdapter?.setList(data?.list)
                loadService.showSuccess()
            }
            //不是第一页
            else -> {
                baseQuickAdapter?.addData(data?.list)
                loadService.showSuccess()
            }
        }
    },{
        loadService.showError("网络错误！")
    })
}