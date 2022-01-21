package com.suchness.landmanage.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.kingja.loadsir.core.LoadService
import com.suchness.deeplearningapp.app.base.BaseFragment
import com.suchness.landmanage.R
import com.suchness.landmanage.app.App
import com.suchness.landmanage.app.ext.*
import com.suchness.landmanage.databinding.FragmentViewPagerBinding
import com.suchness.landmanage.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.include_viewpager.*

/**
 * @author: hejunfeng
 * @date: 2021/12/25 0025
 */
class FileContansFragment : BaseFragment<HomeViewModel, FragmentViewPagerBinding>() {
    private lateinit var loadsir: LoadService<Any>
    var fragments: ArrayList<Fragment> = arrayListOf()
    var mDataList: ArrayList<String> = arrayListOf("图片","视频")
    var fileName : String = ""

    override fun layoutId(): Int {
        return R.layout.fragment_view_pager
    }

    override fun initView(savedInstanceState: Bundle?) {
        loadsir = loadServiceInit(view_pager){
            loadsir.showLoading()
        }
        view_pager.init(this,fragments)
        magic_indicator.bindViewPager2(view_pager,mDataList)
        App.appViewModel?.appColor?.value?.let { setUiTheme(it, viewpager_linear,loadsir) }
        arguments?.let {
            fileName = it.getString("file","");
        }
    }

    override fun lazyLoadData() {
        //设置界面 加载中
        loadsir.showLoading()
        //请求标题数据
        addFragment()
    }

    private fun addFragment() {
        fragments.clear()
        mDataList.let {
            it.forEachIndexed { index, s ->
                fragments.add(FileChildFragment.newInstance(s,fileName))
            }
            magic_indicator.navigator.notifyDataSetChanged()
            view_pager.adapter?.notifyDataSetChanged()
            view_pager.offscreenPageLimit = fragments.size
            loadsir.showSuccess()
        }
    }

    override fun createObserver() {
        App.appViewModel?.appColor?.observeInFragment(this, Observer {
            setUiTheme(it, viewpager_linear,loadsir)
        })
    }
}