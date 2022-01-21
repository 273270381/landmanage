package com.suchness.landmanage.ui.fragment

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ConvertUtils
import com.kingja.loadsir.core.LoadService
import com.suchness.deeplearningapp.app.base.BaseFragment
import com.suchness.deeplearningapp.app.weight.recyclerview.DefineLoadMoreView
import com.suchness.deeplearningapp.app.weight.recyclerview.SpaceItemDecoration
import com.suchness.landmanage.R
import com.suchness.landmanage.app.ext.*
import com.suchness.landmanage.data.been.*
import com.suchness.landmanage.databinding.IncludeListBinding
import com.suchness.landmanage.ui.adapter.AlarmCityAdapter
import com.suchness.landmanage.ui.adapter.AlarmTrafficAdapter
import com.suchness.landmanage.viewmodel.AlarmViewModel
import com.yanzhenjie.recyclerview.SwipeRecyclerView
import kotlinx.android.synthetic.main.include_recyclerview.*
import me.hgj.jetpackmvvm.ext.nav
import me.hgj.jetpackmvvm.ext.navigateAction
import me.hgj.jetpackmvvm.ext.parseState

/**
 * @author: hejunfeng
 * @date: 2021/12/13 0013
 */
class AlarmChildFragment : BaseFragment<AlarmViewModel, IncludeListBinding>() {
    //适配器
    private val cityAdapter: AlarmCityAdapter by lazy { AlarmCityAdapter(arrayListOf()) }
    private val trafficAdapter: AlarmTrafficAdapter by lazy { AlarmTrafficAdapter(arrayListOf()) }
    private lateinit var loadsir: LoadService<Any>
    private var cid = ""
    //recyclerview的底部加载view 因为在首页要动态改变他的颜色，所以加了他这个字段
    private lateinit var footView: DefineLoadMoreView
    private var typelist : MutableList<typeDict> = arrayListOf()
    private var arealist : MutableList<areaDict> = arrayListOf()
    private var devicelist : MutableList<deviceInfo> = arrayListOf()
    private var data : requeryData = requeryData(null,null,null,null,null)
    override fun layoutId(): Int {
        return R.layout.include_list
    }

    override fun initView(savedInstanceState: Bundle?) {
        arguments?.let {
            cid = it.getString("cid","")
        }
        loadsir = loadServiceInit(swipeRefresh){
            //点击重试时触发的操作
            loadsir.showLoading()
            data.let {
                it.type = null
                it.area = null
                it.channel = null
                it.startTime = null
                it.endTime = null
            }
            mViewModel.getData(cid,data)
        }
        when(cid){
            "交通管控"->initTraffic()
            "治安管控"->initCity()
            "城市管理"->initCity()
        }

        //初始化 SwipeRefreshLayout
        swipeRefresh.init {
            //触发刷新监听时请求数据
            mViewModel.pageNum = 1
            mViewModel.getData(cid,data)
        }
        cityAdapter.run {
            setOnItemClickListener{ _, _, p ->
                nav().navigateAction(R.id.action_to_detailfragment, Bundle().apply {
                    var cityInfo = cityAdapter.data.get(p)
                    putParcelable("msg",AlarmMsg(cityInfo.imgPath,null,cityInfo.alarmType,cityInfo.startTime,cityInfo.endTime,cityInfo.channel,cityInfo.area,
                    cityInfo.devDesc,cityInfo.serialNumber,cityInfo.gplLocation))
                })
            }
        }
        trafficAdapter.run {
            setOnItemClickListener{ adapter, view, p ->
                nav().navigateAction(R.id.action_to_detailfragment, Bundle().apply {
                    var trafficInfo = trafficAdapter.data.get(p)
                    putParcelable("msg",AlarmMsg(trafficInfo.imgPath,trafficInfo.averageSpeed.toString(),trafficInfo.alarmType,trafficInfo.playBackBeginTime,trafficInfo.playBackEndTime,trafficInfo.channel,trafficInfo.area,
                        trafficInfo.devDesc,trafficInfo.serialNumber,trafficInfo.gplLocation))
                })
            }
        }
    }

    private fun initTraffic(){
        recyclerView.init(LinearLayoutManager(context), trafficAdapter).let {
            it.addItemDecoration(SpaceItemDecoration(10, ConvertUtils.dp2px(8f)))

            footView = it.initFooter(SwipeRecyclerView.LoadMoreListener {
                //触发加载更多时请求数据
                mViewModel.getData(cid,data)
            })
        }
    }

    private fun initCity(){
        recyclerView.init(LinearLayoutManager(context), cityAdapter).let {
            it.addItemDecoration(SpaceItemDecoration(10, ConvertUtils.dp2px(8f)))

            footView = it.initFooter(SwipeRecyclerView.LoadMoreListener {
                //触发加载更多时请求数据
                mViewModel.getData(cid,data)
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun createObserver() {
        mViewModel.trafficAlramInfo.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            parseState( it ,{ data ->
                loadListData(data, trafficAdapter, loadsir, recyclerView, swipeRefresh)
                //dropdown_menu.setTitle(data.total.toString()+"条报警信息")
                //请求成功
                mViewModel.pageNum++

            })
        })

        mViewModel.policeAlramInfo.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            parseState( it ,{ data ->
                loadListData(data, cityAdapter, loadsir, recyclerView, swipeRefresh)
                //dropdown_menu.setTitle(data.total.toString()+"条报警信息")
                //请求成功
                mViewModel.pageNum++

            })
        })

        mViewModel.cityAlramInfo.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            parseState( it ,{ data ->
                loadListData(data, cityAdapter, loadsir, recyclerView, swipeRefresh)
                //dropdown_menu.setTitle(data.total.toString()+"条报警信息")
                //请求成功
                mViewModel.pageNum++

            })
        })

        mViewModel.typeList.observe(viewLifecycleOwner, Observer {
            parseState(it,{ data ->
                typelist = data
                typelist.add(typeDict(null,"全部"))
            })
            getAlarmData()
        })

        mViewModel.areaList.observe(viewLifecycleOwner, Observer {
            parseState(it,{ data ->
                arealist = data
                arealist.add(areaDict(null,"全部"))
            })
            getAlarmData()
        })
        mViewModel.deviceList.observe(viewLifecycleOwner, Observer {
            parseState(it,{data ->
                devicelist = data
                devicelist.add(0,deviceInfo(null,"全部",null,null,null,null,null,null,null,null,null,null))
            })
            getAlarmData()
        })
        super.createObserver()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getAlarmData(){
        if (typelist.size != 0 && arealist.size != 0 && devicelist.size != 0){
            dropdown_menu.init(this,typelist,devicelist,arealist,swipeRefresh){ type, area,channel, startTime, endTime ->
                data.let { it->
                    it.type = type
                    it.area = area
                    it.channel = channel
                    it.startTime = startTime
                    it.endTime = endTime
                }
                mViewModel.pageNum = 1
                mViewModel.getData(cid, data)
            }
        }
    }

    override fun lazyLoadData() {
        loadsir.showLoading()
        mViewModel.getData(cid,data)
        mViewModel.getDict(cid)
    }

    companion object {
        fun newInstance(cid: String): AlarmChildFragment {
            val args = Bundle()
            args.putString("cid", cid)
            var fragment = AlarmChildFragment()
            fragment.arguments = args
            return fragment
        }
    }
}