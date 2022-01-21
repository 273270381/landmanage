package com.suchness.landmanage.ui.fragment

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ConvertUtils
import com.kingja.loadsir.core.LoadService
import com.suchness.deeplearningapp.app.base.BaseFragment
import com.suchness.deeplearningapp.app.weight.recyclerview.DefineLoadMoreView
import com.suchness.deeplearningapp.app.weight.recyclerview.SpaceItemDecoration
import com.suchness.landmanage.R
import com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_IMAGE_PATH
import com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_VIDEO_PATH
import com.suchness.landmanage.app.ext.*
import com.suchness.landmanage.app.utils.DataUtils
import com.suchness.landmanage.databinding.FragmentFileBinding
import com.suchness.landmanage.ui.adapter.FileAdapter
import com.suchness.landmanage.viewmodel.FileViewModel
import com.yanzhenjie.recyclerview.SwipeRecyclerView
import kotlinx.android.synthetic.main.include_recyclerview.*
import kotlinx.android.synthetic.main.include_toolbar.*
import me.hgj.jetpackmvvm.demo.app.weight.loadCallBack.EmptyCallback
import me.hgj.jetpackmvvm.ext.nav
import me.hgj.jetpackmvvm.ext.navigateAction

/**
 * @author: hejunfeng
 * @date: 2021/12/25 0025
 */
class FileFragment : BaseFragment<FileViewModel, FragmentFileBinding>() {
    private val fileAdapter: FileAdapter by lazy { FileAdapter(arrayListOf()) }
    private lateinit var loadsir: LoadService<Any>
    //recyclerview的底部加载view 因为在首页要动态改变他的颜色，所以加了他这个字段
    private lateinit var footView: DefineLoadMoreView
    private var filePicList: MutableList<String> = arrayListOf()
    private var fileVideoList: MutableList<String> = arrayListOf()

    override fun layoutId(): Int {
        return R.layout.fragment_file
    }

    override fun initView(savedInstanceState: Bundle?) {

        loadsir = loadServiceInit(swipeRefresh){
            //点击重试时触发的操作
            loadsir.showLoading()
            getData()
        }

        recyclerView.init(LinearLayoutManager(context),fileAdapter ).let{
            it.addItemDecoration(SpaceItemDecoration(10, ConvertUtils.dp2px(8f)))
            footView = it.initFooter(SwipeRecyclerView.LoadMoreListener {
                //触发加载更多时请求数据
                getData()
            })
        }

        swipeRefresh.init {
            //触发刷新监听时请求数据
            mViewModel.pageNum = 1
            getData()
        }

        fileAdapter.run {
            setOnItemClickListener{ _,_,p ->
                nav().navigateAction(R.id.action_to_file_contansfragment,Bundle().apply {
                    putString("file",fileAdapter.data[p])
                })
            }
        }
    }

    override fun lazyLoadData() {
        loadsir.showLoading()
        getData()
    }

    fun getData(){
        filePicList = DataUtils.getPathList(DEFAULT_SAVE_IMAGE_PATH)
        fileVideoList = DataUtils.getPathList(DEFAULT_SAVE_VIDEO_PATH)
        for (str in filePicList){
            if (!fileVideoList.contains(str)){
                fileVideoList.add(str)
            }
        }
        if (fileVideoList.size == 0) {
            loadsir.showCallback(EmptyCallback::class.java)
        } else {
            loadsir.showSuccess()
        }
        swipeRefresh.isRefreshing = false
        fileAdapter.setList(fileVideoList)
    }

    override fun createObserver() {
//        mViewModel.deviceList.observe(viewLifecycleOwner, Observer {
//            parseState(it,{data ->
//                loadListData(data, fileAdapter, loadsir, recyclerView, swipeRefresh)
//                //请求成功
//                mViewModel.pageNum++
//            })
//        })
        super.createObserver()
    }
}