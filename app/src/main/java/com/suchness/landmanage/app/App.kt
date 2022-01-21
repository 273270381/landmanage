package com.suchness.landmanage.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.Log
import androidx.multidex.BuildConfig
import androidx.multidex.MultiDex
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.kingja.loadsir.callback.SuccessCallback
import com.kingja.loadsir.core.LoadSir
import com.suchness.deeplearningapp.app.event.AppViewModel
import com.suchness.deeplearningapp.ui.activity.ErrorActivity
import com.suchness.deeplearningapp.ui.activity.MainActivity
import com.suchness.deeplearningapp.ui.activity.WelcomeActivity
import com.suchness.landmanage.R
import com.suchness.landmanage.app.utils.CacheUtil
import com.suchness.landmanage.app.utils.CommonUtil
import com.suchness.landmanage.data.been.db.DBManager
import com.suchness.landmanage.data.been.db.PicFilePath
import com.suchness.landmanage.data.been.db.Verifcode
import com.suchness.landmanage.data.been.db.VideoFilePath
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta
import com.tencent.bugly.beta.upgrade.UpgradeStateListener
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import com.videogo.openapi.EZOpenSDK
import com.videogo.util.LogUtil
import me.hgj.jetpackmvvm.base.BaseApp
import me.hgj.jetpackmvvm.demo.app.weight.loadCallBack.EmptyCallback
import me.hgj.jetpackmvvm.demo.app.weight.loadCallBack.ErrorCallback
import me.hgj.jetpackmvvm.demo.app.weight.loadCallBack.LoadingCallback

/**
 * @author: hejunfeng
 * @date: 2021/9/23 0023
 */

//Application全局的ViewModel，里面存放了一些账户信息，基本配置信息等
//val appViewModel: AppViewModel by lazy { App.appViewModelInstance }


class App : BaseApp(){

    companion object {
        var instance : App?= null
        var appViewModel: AppViewModel?=null
    }

    override fun onCreate() {
        super.onCreate()
        initcash()
        initBugly()
    }

    private fun initEzSdk(){
        /**
         * sdk日志开关，正式发布需要去掉
         */
        EZOpenSDK.showSDKLog(true)

        /**
         * 设置是否支持P2P取流,详见api
         */
        EZOpenSDK.enableP2P(true)

        /**
         * APP_KEY请替换成自己申请的
         */
        EZOpenSDK.initLib(this, "bec27f333fd04a95a352bec49d466754")
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.videogo.action.ADD_DEVICE_SUCCESS_ACTION")
        intentFilter.addAction("com.action.OAUTH_SUCCESS_ACTION")
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        val receiver: EzvizBroadcastReceiver = EzvizBroadcastReceiver()
        registerReceiver(receiver, intentFilter)
    }

    private fun initcash() {
        DBManager.init(this)
        DBManager.getInstance().create(PicFilePath::class.java)
        DBManager.getInstance().create(Verifcode::class.java)
        DBManager.getInstance().create(VideoFilePath::class.java)
        MMKV.initialize(this.filesDir.absolutePath + "/mmkv")
        SDKInitializer.initialize(this)
        SDKInitializer.setCoordType(CoordType.BD09LL);
        instance = this
        appViewModel = getAppViewModelProvider().get(AppViewModel::class.java)
        //界面加载管理 初始化
        LoadSir.beginBuilder()
            .addCallback(LoadingCallback())//加载
            .addCallback(ErrorCallback())//错误
            .addCallback(EmptyCallback())//空
            .setDefaultCallback(SuccessCallback::class.java)//设置默认加载状态页
            .commit()

        //防止项目崩溃，崩溃后打开错误界面
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true)//是否启用CustomActivityOnCrash崩溃拦截机制 必须启用！不然集成这个库干啥？？？
            .showErrorDetails(false) //是否必须显示包含错误详细信息的按钮 default: true
            .showRestartButton(false) //是否必须显示“重新启动应用程序”按钮或“关闭应用程序”按钮default: true
            .logErrorOnRestart(false) //是否必须重新堆栈堆栈跟踪 default: true
            .trackActivities(true) //是否必须跟踪用户访问的活动及其生命周期调用 default: false
            .minTimeBetweenCrashesMs(2000) //应用程序崩溃之间必须经过的时间 default: 3000
            .restartActivity(WelcomeActivity::class.java) // 重启的activity
            .errorActivity(ErrorActivity::class.java) //发生错误跳转的activity
            .apply()
        CacheUtil.setTifNum(25);
        CacheUtil.setFileNum(107);
        initEzSdk()
    }

    private fun initBugly() {
        if(BuildConfig.DEBUG){
//            return
        }
        /**
         * true表示app启动自动初始化升级模块; false不会自动初始化;
         * 开发者如果担心sdk初始化影响app启动速度，可以设置为false，
         * 在后面某个时刻手动调用Beta.init(getApplicationContext(),false);
         */
        Beta.autoInit = true
        /**
         * true表示初始化时自动检查升级; false表示不会自动检查升级,需要手动调用Beta.checkUpgrade()方法;
         */

        Beta.autoCheckUpgrade = false;
        /**
         * 设置升级检查周期为60s(默认检查周期为0s)，60s内SDK不重复向后台请求策略);
         */
//        Beta.upgradeCheckPeriod = 60*1000
        /**
         * 设置启动延时为1s（默认延时3s），APP启动1s后初始化SDK，避免影响APP启动速度;
         */
        Beta.initDelay = 1*1000
        /**
         * 设置通知栏大图标，largeIconId为项目中的图片资源;
         */
        Beta.largeIconId = R.mipmap.papers
        /**
         * 设置状态栏小图标，smallIconId为项目中的图片资源Id;
         */
        Beta.smallIconId = R.mipmap.papers
        /**
         * 设置更新弹窗默认展示的banner，defaultBannerId为项目中的图片资源Id;
         * 当后台配置的banner拉取失败时显示此banner，默认不设置则展示“loading“;
         */
        Beta.defaultBannerId = R.mipmap.papers
        /**
         * 设置sd卡的Download为更新资源保存目录;
         * 后续更新资源会保存在此目录，需要在manifest中添加WRITE_EXTERNAL_STORAGE权限;
         */
        Beta.storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        /**
         * 已经确认过的弹窗在APP下次启动自动检查更新时会再次显示;
         */
        Beta.showInterruptedStrategy = false
        /**
         * 只允许在MainActivity上显示更新弹窗，其他activity上不显示弹窗; 不设置会默认所有activity都可以显示弹窗;
         */
//        Beta.canShowUpgradeActs.add(MainActivity::class.java)
        // 获取当前进程名
        val processName = CommonUtil.getProcessName(android.os.Process.myPid())
        // 设置是否为上报进程
        val strategy = CrashReport.UserStrategy(applicationContext)
        if (processName === applicationContext.packageName) {
            strategy.isUploadProcess = false
        }
        Beta.upgradeStateListener = upgradeStateListener
        // 自定义更新布局要设置在 init 之前
        // R.layout.layout_upgrade_dialog 文件要注意两点
        // 注意1: 半透明背景要自己加上
        // 注意2: 即使自定义的弹窗不需要title, info等这些信息, 也需要将对应的tag标出出来, 一共有5个
        Beta.upgradeDialogLayoutId = R.layout.layout_upgrade_dialog
        //Beta.dialogFullScreen = true
        // CrashReport.initCrashReport(applicationContext, Constant.BUGLY_ID, BuildConfig.DEBUG, strategy)
        Bugly.init(applicationContext, "5df65ff282",false, strategy)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
        // 安装 Tinker
        Beta.installTinker()
    }
    private val upgradeStateListener = object : UpgradeStateListener {
        override fun onUpgradeFailed(p0: Boolean) {
            Log.d("hjf","onUpgradeFailed: "+p0)
        }

        override fun onUpgradeSuccess(p0: Boolean) {
            Log.d("hjf","onUpgradeSuccess: "+p0)
        }

        override fun onUpgradeNoVersion(p0: Boolean) {
            Log.d("hjf","onUpgradeNoVersion: "+p0)
        }

        override fun onUpgrading(p0: Boolean) {
            Log.d("hjf","onUpgrading: "+p0)
        }

        override fun onDownloadCompleted(p0: Boolean) {
        }

    }


    private class EzvizBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            LogUtil.i("TAG", "action = $action")
            //if (action.equals(Constant.OAUTH_SUCCESS_ACTION)){
            if (action == "com.action.OAUTH_SUCCESS_ACTION") {
                Log.i("TAG", "onReceive: OAUTH_SUCCESS_ACTION")
                val i = Intent(context, MainActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                /*******   获取登录成功之后的EZAccessToken对象    */
                context.startActivity(i)
            }
        }
    }

}