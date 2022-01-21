package com.suchness.deeplearningapp.app.base

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.gyf.barlibrary.ImmersionBar
import com.suchness.landmanage.app.ext.dismissLoadingExt
import com.suchness.landmanage.app.ext.showLoadingExt
import me.hgj.jetpackmvvm.base.activity.BaseVmActivity
import me.hgj.jetpackmvvm.base.activity.BaseVmDbActivity
import me.hgj.jetpackmvvm.base.viewmodel.BaseViewModel

/**
 * @author: hejunfeng
 * @date: 2021/9/24 0024
 */
abstract class BaseActivity <VM : BaseViewModel, DB : ViewDataBinding> : BaseVmDbActivity<VM,DB>() {
    abstract override fun layoutId(): Int

    abstract override fun initView(savedInstanceState: Bundle?)

    /**
     * 创建liveData观察者
     */
    override fun createObserver() {}

    /**
     * 打开等待框
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun showLoading(message: String) {
        showLoadingExt(message)
    }

    /**
     * 关闭等待框
     */
    override fun dismissLoading() {
        dismissLoadingExt()
    }

    override fun initMersionBar() {
        super.initMersionBar()
        ImmersionBar.with(this).init();
    }

    override fun onDestroy() {
        super.onDestroy()
        ImmersionBar.with(this).destroy();
    }
}