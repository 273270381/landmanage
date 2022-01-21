package com.suchness.deeplearningapp.ui.activity

import android.Manifest
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.blankj.utilcode.util.ToastUtils
import com.suchness.deeplearningapp.app.base.BaseActivity
import com.suchness.landmanage.R
import com.suchness.landmanage.app.App
import com.suchness.landmanage.app.utils.StatusBarUtil
import com.suchness.landmanage.bus.BusEvent
import com.suchness.landmanage.bus.RxbusSingle
import com.suchness.landmanage.databinding.ActivityMainBinding
import com.suchness.landmanage.viewmodel.MainViewModel
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tencent.bugly.beta.Beta
import me.hgj.jetpackmvvm.network.manager.NetState


class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {
    var exitTime = 0L

    override fun layoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView(savedInstanceState: Bundle?) {
        //进入首页检查更新
        Beta.checkUpgrade()
        val rxPermissions = RxPermissions(this)
        rxPermissions.request(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_LOGS,
            Manifest.permission.VIBRATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO
        ).subscribe {
            if (it){
                Log.d("hjf","initData")

            }else{

            }
        }
        initData()
    }


    private fun initData(){
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val nav = Navigation.findNavController(this@MainActivity, R.id.host_fragment)
                if (nav.currentDestination != null){
                    if (nav.currentDestination!!.id == R.id.homeFragment || nav.currentDestination!!.id == R.id.loginFragment){
                        //是主页
                        if (System.currentTimeMillis() - exitTime > 2000) {
                            ToastUtils.showShort("再按一次退出程序")
                            exitTime = System.currentTimeMillis()
                        } else {
                            finish()
                        }
                    }else{
                        //如果当前界面不是主页，那么直接调用返回即可
                        nav.navigateUp()
                    }
                }
            }
        })

        App.appViewModel?.appColor?.value?.let {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            supportActionBar?.setBackgroundDrawable(ColorDrawable(it))
            StatusBarUtil.setColor(this, it, 0) }
    }

    override fun createObserver() {
        RxbusSingle.INSTANCE.toObservable(BusEvent::class.java).subscribe(
            { event ->
                if (event.name.equals("recreate")){
                    this@MainActivity.recreate()
                }
            },
            { throwable -> Log.d("hjf", "error!") }
        )
        App.appViewModel?.appColor?.observeInActivity(this, Observer {
            supportActionBar?.setBackgroundDrawable(ColorDrawable(it))
            StatusBarUtil.setColor(this, it, 0)
        })
    }

    override fun onNetworkStateChanged(netState: NetState) {
        super.onNetworkStateChanged(netState)
        if (netState.isSuccess) {
//            Toast.makeText(applicationContext, "我特么终于有网了啊!", Toast.LENGTH_SHORT).show()
        } else {
//            Toast.makeText(applicationContext, "我特么怎么断网了!", Toast.LENGTH_SHORT).show()
        }
    }

}