package com.suchness.landmanage.app.ext

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.suchness.deeplearningapp.app.weight.viewpager.ScaleTransitionPagerTitleView
import com.suchness.landmanage.R
import com.suchness.landmanage.app.utils.SettingUtil
import com.suchness.landmanage.app.weight.dropDownMenu.ConstellationAdapter
import com.suchness.landmanage.app.weight.dropDownMenu.DropDownMenu
import com.suchness.landmanage.data.been.areaDict
import com.suchness.landmanage.data.been.deviceInfo
import com.suchness.landmanage.data.been.typeDict
import kotlinx.android.synthetic.main.custom_layout.view.*
import me.hgj.jetpackmvvm.base.Ktx
import me.hgj.jetpackmvvm.ext.util.toHtml
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import java.util.stream.Collectors

/**
 * @author: hejunfeng
 * @date: 2021/12/13 0013
 */
fun MagicIndicator.bindViewPager2(
    viewPager: ViewPager2,
    mStringList: List<String> = arrayListOf(),
    action: (index: Int) -> Unit = {}){
    var commonNavigator = CommonNavigator(Ktx.app)
    commonNavigator.adapter = object : CommonNavigatorAdapter(){
        override fun getCount(): Int {
            return mStringList.size
        }

        override fun getTitleView(context: Context?, index: Int): ScaleTransitionPagerTitleView? {
            return Ktx.app?.let {
                ScaleTransitionPagerTitleView(it).apply {
                    //设置文本
                    text = mStringList[index].toHtml()
                    //字体大小
                    textSize = 17f
                    //未选中颜色
                    normalColor = Color.WHITE
                    //选中颜色
                    selectedColor = Color.WHITE
                    //点击事件
                    setOnClickListener {
                        viewPager.currentItem = index
                        action.invoke(index)
                    }
                }
            }
        }

        override fun getIndicator(p0: Context?): IPagerIndicator {
            return LinePagerIndicator(context).apply {
                mode = LinePagerIndicator.MODE_EXACTLY
                //线条的宽高度
                lineHeight = UIUtil.dip2px(Ktx.app, 3.0).toFloat()
                lineWidth = UIUtil.dip2px(Ktx.app, 30.0).toFloat()
                //线条的圆角
                roundRadius = UIUtil.dip2px(Ktx.app, 6.0).toFloat()
                startInterpolator = AccelerateInterpolator()
                endInterpolator = DecelerateInterpolator(2.0f)
                //线条的颜色
                setColors(Color.WHITE)
            }
        }
    }
    this.navigator = commonNavigator
    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            this@bindViewPager2.onPageSelected(position)
            action.invoke(position)
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            this@bindViewPager2.onPageScrolled(position,positionOffset, positionOffsetPixels)
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            this@bindViewPager2.onPageScrollStateChanged(state)
        }
    })
}
@RequiresApi(Build.VERSION_CODES.N)
fun DropDownMenu.init(
    fragment: Fragment,
    typeData: MutableList<typeDict>?,
    deviceList: MutableList<deviceInfo>?,
    areaData: MutableList<areaDict>?,
    swipeRefreshLayout: SwipeRefreshLayout, refresh:(type : Int?, area: String?, channelnum:String?, startTime: String?, endTime: String?) -> Unit){
    var constellationPosition = 0;
    var constellationPosition2 = 0;
    var spinnerPosition = 0;
    val constellationView = fragment.layoutInflater.inflate(R.layout.custom_layout,null)
    var header = arrayOf(fragment.getString(R.string.filter))
    val dropDownAdapterType = ConstellationAdapter(context, typeData?.stream()?.map(typeDict :: value)?.collect(Collectors.toList()))
    val dropDownAdapterArea = ConstellationAdapter(context, areaData?.stream()?.map(areaDict :: fullname)?.collect(Collectors.toList()))
    constellationView.constellation.adapter = dropDownAdapterType
    constellationView.constellation2.adapter = dropDownAdapterArea
    if (deviceList != null) {
        var devDesc = deviceList.stream().map { item -> item.devDesc }.collect(Collectors.toList())
        constellationView.spinner.setItems(devDesc)
    }
    constellationView.spinner.setOnItemSelectedListener { spinner, position, id, item ->
        spinnerPosition = position
    }
    val popupViews: MutableList<View> = java.util.ArrayList()
    popupViews.add(constellationView)
    val convertview = TextView(context)
    convertview.setBackgroundResource(R.color.transparent_75)
    setDropDownMenu(listOf(*header),popupViews, convertview)
    post {
        swipeRefreshLayout.scrollY = SettingUtil.getObsetData(tabMenuView.measuredHeight)
    }
    fragment.context?.let {
        constellationView.startTime.initCustomTimePicker(it)
        constellationView.endTime.initCustomTimePicker(it)
    }
    constellationView.constellation.setOnItemClickListener { parent, view, position, id ->
        dropDownAdapterType.setCheckItem(position);
        constellationPosition = position
    }
    constellationView.constellation2.setOnItemClickListener { parent, view, position, id ->
        dropDownAdapterArea.setCheckItem(position);
        constellationPosition2 = position
    }
    constellationView.cancel.setOnClickListener {
        closePopuMenu()
    }
    constellationView.ok.setOnClickListener {
        if (TextUtils.isEmpty(startTime.text) || TextUtils.isEmpty(endTime.text)){
            if (areaData?.get(constellationPosition2)?.fullname.equals("全部")){
                var s = typeData?.get(constellationPosition)?.key
                if (s?.equals("null") == true){

                }
                refresh.invoke(typeData?.get(constellationPosition)?.key,null,deviceList?.get(spinnerPosition)?.channelNo,null,null)
            }else{
                refresh.invoke(typeData?.get(constellationPosition)?.key,areaData?.get(constellationPosition2)?.fullname,deviceList?.get(spinnerPosition)?.channelNo,null,null)
            }
        }else{
            if (areaData?.get(constellationPosition2)?.fullname.equals("全部")){
                refresh.invoke(typeData?.get(constellationPosition)?.key,null,deviceList?.get(spinnerPosition)?.channelNo,startTime.text.toString().trim(),endTime.text.toString().trim())
            }else{
                refresh.invoke(typeData?.get(constellationPosition)?.key,areaData?.get(constellationPosition2)?.fullname,deviceList?.get(spinnerPosition)?.channelNo,startTime.text.toString().trim(),endTime.text.toString().trim())
            }
        }
        closePopuMenu()
    }
}