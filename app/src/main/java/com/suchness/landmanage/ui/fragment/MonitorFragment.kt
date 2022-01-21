package com.suchness.landmanage.ui.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ConvertUtils
import com.kingja.loadsir.core.LoadService
import com.suchness.deeplearningapp.app.base.BaseFragment
import com.suchness.deeplearningapp.app.weight.recyclerview.DefineLoadMoreView
import com.suchness.deeplearningapp.app.weight.recyclerview.SpaceItemDecoration
import com.suchness.landmanage.R
import com.suchness.landmanage.app.ext.*
import com.suchness.landmanage.app.utils.AppOperator
import com.suchness.landmanage.app.weight.playback.RemoteListContant
import com.suchness.landmanage.databinding.FragmentMonitorBinding
import com.suchness.landmanage.ui.activity.TestActivity
import com.suchness.landmanage.ui.adapter.DeviceAdapter
import com.suchness.landmanage.viewmodel.MonitorViewModel
import com.videogo.constant.IntentConsts
import com.videogo.openapi.EZOpenSDK
import com.videogo.openapi.bean.EZCameraInfo
import com.videogo.openapi.bean.EZDeviceInfo
import com.videogo.util.DateTimeUtil
import com.yanzhenjie.recyclerview.SwipeRecyclerView
import kotlinx.android.synthetic.main.include_recyclerview.*
import kotlinx.android.synthetic.main.include_toolbar.*
import me.hgj.jetpackmvvm.ext.nav
import me.hgj.jetpackmvvm.ext.navigateAction
import me.hgj.jetpackmvvm.ext.parseState
import java.lang.Exception
import java.util.stream.Collectors

/**
 * @author: hejunfeng
 * @date: 2021/12/20 0020
 */
class MonitorFragment : BaseFragment<MonitorViewModel, FragmentMonitorBinding>() {
    private val deviceAdapter: DeviceAdapter by lazy { DeviceAdapter(arrayListOf()) }
    private lateinit var loadsir: LoadService<Any>
    private var cid = ""
    //recyclerview的底部加载view 因为在首页要动态改变他的颜色，所以加了他这个字段
    private lateinit var footView: DefineLoadMoreView


    fun lazy() {
        DeviceAdapter(arrayListOf())
    }

    override fun layoutId(): Int {
        return R.layout.fragment_monitor
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun initView(savedInstanceState: Bundle?) {
        var cameraList : MutableList<EZCameraInfo> = arrayListOf()
        var devList : MutableList<EZDeviceInfo> = arrayListOf()
        AppOperator.runOnThread(Runnable {
            try {
                devList = EZOpenSDK.getInstance().getDeviceList(0,20)
                for (dev in devList!!){
                    cameraList = dev.cameraInfoList
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        })

        arguments?.let {
            cid = it.getString("cid","")
        }
        loadsir = loadServiceInit(swipeRefresh){
            //点击重试时触发的操作
            loadsir.showLoading()
            mViewModel.getData()
        }

        recyclerView.init(LinearLayoutManager(context), deviceAdapter).let {
            it.addItemDecoration(SpaceItemDecoration(0, ConvertUtils.dp2px(8f)))

            footView = it.initFooter(SwipeRecyclerView.LoadMoreListener {
                //触发加载更多时请求数据
                mViewModel.getData()
            })
        }
        //初始化 SwipeRefreshLayout
        swipeRefresh.init {
            //触发刷新监听时请求数据
            mViewModel.pageNum = 1
            mViewModel.getData()
        }
        deviceAdapter.run {
            setOnClick(){ item, view, _ ->
                when(view?.id){
                    R.id.item_play_btn -> {
                        //播放
                        nav().navigateAction(R.id.action_to_realPlayFragment,Bundle().apply {
                            cameraList.let {  devList->
                                var cameraInfos = devList.stream().filter { it.cameraNo == item.channelNo?.toInt()}.collect(
                                    Collectors.toList())
                                if (cameraInfos.size > 0){
                                    putParcelable(IntentConsts.EXTRA_CAMERA_INFO,cameraInfos[0])
                                }
                            }
                            putParcelable(IntentConsts.EXTRA_DEVICE_INFO,devList[0])
                        })
                    }
                    R.id.playback -> {
                        nav().navigateAction(R.id.action_to_playbackfragment,Bundle().apply {
                            cameraList.let {  devList->
                                var cameraInfos = devList.stream().filter { it.cameraNo == item.channelNo?.toInt()}.collect(
                                    Collectors.toList())
                                putParcelable(IntentConsts.EXTRA_CAMERA_INFO,cameraInfos[0])
                            }
                            putParcelable(IntentConsts.EXTRA_DEVICE_INFO,devList[0])
                        })
                    }
                }
            }
        }

    }

    override fun createObserver() {
        mViewModel.devInfo.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            parseState( it ,{ data ->
                loadListData(data, deviceAdapter, loadsir, recyclerView, swipeRefresh)
                //请求成功
                mViewModel.pageNum++

            })
        })
        super.createObserver()
    }

    override fun lazyLoadData() {
        loadsir.showLoading()
        mViewModel.getData()
    }
}