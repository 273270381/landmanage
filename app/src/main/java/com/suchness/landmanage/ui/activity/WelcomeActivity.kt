package com.suchness.deeplearningapp.ui.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import com.suchness.deeplearningapp.app.base.BaseActivity
import com.suchness.deeplearningapp.app.weight.banner.WelcomeBannerViewHolder
import com.suchness.landmanage.R
import com.suchness.landmanage.app.utils.CacheUtil
import com.suchness.landmanage.app.utils.SettingUtil
import com.suchness.landmanage.databinding.ActivityWelcomeBinding
import com.zhpan.bannerview.BannerViewPager
import kotlinx.android.synthetic.main.activity_welcome.*
import me.hgj.jetpackmvvm.base.viewmodel.BaseViewModel
import me.hgj.jetpackmvvm.ext.view.visible

/**
 * @author: hejunfeng
 * @date: 2021/9/24 0024
 */
class WelcomeActivity : BaseActivity<BaseViewModel, ActivityWelcomeBinding>() {

    private var resList = arrayOf("如是", "地球")

    private lateinit var mViewPager: BannerViewPager<String, WelcomeBannerViewHolder>

    override fun layoutId(): Int {
        return R.layout.activity_welcome
    }

    override fun initView(savedInstanceState: Bundle?) {
        //防止出现按Home键回到桌面时，再次点击重新进入该界面bug
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT !== 0) {
            finish()
            return
        }
        mDatabind.click = ProxyClick()
        welcome_baseview.setBackgroundColor(SettingUtil.getColor(this))
        mViewPager = findViewById(R.id.banner_view)
//        if (CacheUtil.isFirst()) {
//            //是第一次打开App 显示引导页
//            welcome_image.gone()
//            mViewPager.apply {
//                adapter = WelcomeBannerAdapter()
//                setLifecycleRegistry(lifecycle)
//                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//                    override fun onPageSelected(position: Int) {
//                        super.onPageSelected(position)
//                        if (position == resList.size - 1) {
//                            welcomeJoin.visible()
//                        } else {
//                            welcomeJoin.gone()
//                        }
//                    }
//                })
//                create(resList.toList())
//            }
//        }else {
//            //不是第一次打开App 0.3秒后自动跳转到主页
//            welcome_image.visible()
//            mViewPager.postDelayed({
//                startActivity(Intent(this, MainActivity::class.java))
//                finish()
//                //带点渐变动画
//                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//            }, 1000)
//        }
        welcome_image.visible()
        mViewPager.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            //带点渐变动画
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 1000)
        mDatabind.version.text = "版本 "+getAppVersionName(this);
    }

    /**
     * 返回当前程序版本号
     */
    fun getAppVersionCode(context: Context): String? {
        var versioncode = 0
        try {
            val pm: PackageManager = context.packageManager
            val pi: PackageInfo = pm.getPackageInfo(context.packageName, 0)
            // versionName = pi.versionName;
            versioncode = pi.versionCode
        } catch (e: Exception) {
            Log.e("VersionInfo", "Exception", e)
        }
        return versioncode.toString() + ""
    }

    /**
     * 返回当前程序版本名
     */
    fun getAppVersionName(context: Context): String? {
        var versionName: String? = null
        try {
            val pm: PackageManager = context.packageManager
            val pi: PackageInfo = pm.getPackageInfo(context.packageName, 0)
            versionName = pi.versionName
        } catch (e: Exception) {
            Log.e("VersionInfo", "Exception", e)
        }
        return versionName
    }

    inner class ProxyClick {
        fun toMain() {
            CacheUtil.setFirst(false)
            startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
            finish()
            //带点渐变动画
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}