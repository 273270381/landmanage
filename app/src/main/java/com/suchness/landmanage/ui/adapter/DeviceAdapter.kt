package com.suchness.landmanage.ui.adapter

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.suchness.deeplearningapp.app.weight.customview.CollectView
import com.suchness.landmanage.R
import com.suchness.landmanage.app.ext.setAdapterAnimation
import com.suchness.landmanage.app.utils.SettingUtil
import com.suchness.landmanage.data.been.cityInfo
import com.suchness.landmanage.data.been.deviceInfo
import com.suchness.landmanage.data.been.trafficInfo
import com.suchness.landmanage.generated.callback.OnClickListener
import me.hgj.jetpackmvvm.ext.util.toHtml


class DeviceAdapter(data: MutableList<deviceInfo>?) : BaseDelegateMultiAdapter<deviceInfo, BaseViewHolder>(data) {
    private var showTag = false//是否展示标签 tag 一般主页才用的到

    private var clickAction: (item: deviceInfo, v: View?, position: Int) -> Unit = {_: deviceInfo,_: View?, _: Int ->}

    constructor(data: MutableList<deviceInfo>?, showTag: Boolean) : this(data) {
        this.showTag = showTag
    }

    init {
        setAdapterAnimation(SettingUtil.getListMode())
        // 第一步，设置代理
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<deviceInfo>() {
            override fun getItemType(data: List<deviceInfo>, position: Int): Int {
                //根据是否有图片 判断为文章还是项目，好像有点low的感觉。。。我看实体类好像没有相关的字段，就用了这个，也有可能是我没发现
                //return if (TextUtils.isEmpty(data[position].envelopePic)) Ariticle else Project
                return 1
            }
        })
        // 第二步，绑定 item 类型
        getMultiTypeDelegate()?.let {
            it.addItemType(1, R.layout.cameralist_small_item)
        }
    }

    override fun convert(helper: BaseViewHolder, item: deviceInfo) {
        when (helper.itemViewType) {
            1 -> {
                //文章布局的赋值
                item.run {
                    helper.setText(R.id.devDesc,devDesc)
                    helper.setText(R.id.address,address)
                    helper.setText(R.id.devModel,deviceModel)
                    if (isOnline == "1"){
                        Glide.with(context).load(picUrl).apply(RequestOptions.bitmapTransform( RoundedCorners(20)))
                            .transition(DrawableTransitionOptions.withCrossFade(500)).into(helper.getView(R.id.url))
                        helper.setGone(R.id.item_offline,true)
                    }else{
                        Glide.with(context).load(context.getDrawable(R.drawable.my_cover)).apply(RequestOptions.bitmapTransform( RoundedCorners(20)))
                            .transition(DrawableTransitionOptions.withCrossFade(500)).into(helper.getView(R.id.url))
                        helper.setGone(R.id.item_offline,false)
                    }

                    helper.getView<Button>(R.id.playback).setOnClickListener(object : View.OnClickListener{
                        override fun onClick(v: View?) {
                            clickAction.invoke(item,v,helper.adapterPosition)
                        }
                    })

                    helper.getView<ImageButton>(R.id.item_play_btn).setOnClickListener(object : View.OnClickListener{
                        override fun onClick(v: View?) {
                            clickAction.invoke(item,v,helper.adapterPosition)
                        }
                    })
                }
            }
        }
    }

    fun setOnClick(onClickAction: (item: deviceInfo, v: View?, position: Int) -> Unit){
        this.clickAction = onClickAction
    }
}


