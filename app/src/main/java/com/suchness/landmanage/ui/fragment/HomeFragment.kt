package com.suchness.landmanage.ui.fragment

import android.os.Bundle
import com.suchness.deeplearningapp.app.base.BaseFragment
import com.suchness.landmanage.R
import com.suchness.landmanage.app.App
import com.suchness.landmanage.app.ext.init
import com.suchness.landmanage.app.ext.initMain
import com.suchness.landmanage.app.ext.interceptLongClick
import com.suchness.landmanage.app.ext.setUiTheme
import com.suchness.landmanage.databinding.FragmentHomeBinding
import com.suchness.landmanage.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * @author: hejunfeng
 * @date: 2021/9/24 0024
 */
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {
    override fun layoutId(): Int {
        return R.layout.fragment_home
    }

    override fun initView(savedInstanceState: Bundle?) {
        mainViewpager.initMain(this)
        mainBottom.init {
            when(it) {
                R.id.menu_alarm -> mainViewpager.setCurrentItem(0,false)
                R.id.menu_tif -> mainViewpager.setCurrentItem(1, false)
                R.id.menu_monitor -> mainViewpager.setCurrentItem(2, false)
                R.id.menu_file -> mainViewpager.setCurrentItem(3, false)
            }
        }
        mainBottom.interceptLongClick(R.id.menu_alarm,R.id.menu_tif,R.id.menu_monitor,R.id.menu_file)
    }

    override fun createObserver() {
        App.appViewModel?.appColor?.observeInFragment(this) {
            setUiTheme(it, mainBottom)
        }
    }
}