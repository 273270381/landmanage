package com.suchness.landmanage.app.ext

import androidx.fragment.app.Fragment
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.UiSettings
import kotlinx.android.synthetic.main.fragment_baidumap.*
import kotlinx.android.synthetic.main.fragment_map.*

/**
 * @author: hejunfeng
 * @date: 2021/12/28 0028
 */
fun MapView.init(fragment: Fragment) : BaiduMap {
    fragment.let {
        showZoomControls(false)
        // 不显示百度地图Logo
        removeViewAt(1)
        //初始化位置
        val mBaiduMap = it.map_view.map
        mBaiduMap.let {
            it.isTrafficEnabled = true
            val settings: UiSettings = it.getUiSettings()
            settings.isCompassEnabled = false
            settings.isOverlookingGesturesEnabled = false
            it.mapType = BaiduMap.MAP_TYPE_NORMAL
            it.isMyLocationEnabled = true
        }
        return mBaiduMap
    }
}