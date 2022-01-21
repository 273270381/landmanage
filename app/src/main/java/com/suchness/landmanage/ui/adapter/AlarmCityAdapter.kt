package com.suchness.landmanage.ui.adapter

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
import me.hgj.jetpackmvvm.ext.util.toHtml


class AlarmCityAdapter(data: MutableList<cityInfo>?) : BaseDelegateMultiAdapter<cityInfo, BaseViewHolder>(data) {
    private val police = 1//治安管控
    private val city = 2//城市管理
    private var showTag = false//是否展示标签 tag 一般主页才用的到

    private var collectAction: (item: cityInfo, v: CollectView, position: Int) -> Unit =
        { _: cityInfo, _: CollectView, _: Int -> }

    constructor(data: MutableList<cityInfo>?, showTag: Boolean) : this(data) {
        this.showTag = showTag
    }

    init {
        setAdapterAnimation(SettingUtil.getListMode())
        // 第一步，设置代理
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<cityInfo>() {
            override fun getItemType(data: List<cityInfo>, position: Int): Int {
                //根据是否有图片 判断为文章还是项目，好像有点low的感觉。。。我看实体类好像没有相关的字段，就用了这个，也有可能是我没发现
                //return if (TextUtils.isEmpty(data[position].envelopePic)) Ariticle else Project
                return police
            }
        })
        // 第二步，绑定 item 类型
        getMultiTypeDelegate()?.let {
            it.addItemType(police, R.layout.item_alarm)
        }
    }

    override fun convert(helper: BaseViewHolder, item: cityInfo) {
        when (helper.itemViewType) {
            police -> {
                //文章布局的赋值
                item.run {
                    helper.setText(R.id.devDesc,devDesc)
                    helper.setText(R.id.area, area?.toHtml())
                    helper.setText(R.id.alarmtype,alarmType)
                    helper.setText(R.id.time, startTime?.toHtml())
                    var url = imgPath?.split(",")?.get(0)
                    Glide.with(context).load(url).apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                        .transition(DrawableTransitionOptions.withCrossFade(500)).into(helper.getView(R.id.img))
                }
            }
        }
    }

    fun setCollectClick(inputCollectAction: (item: cityInfo, v: CollectView, position: Int) -> Unit) {
        this.collectAction = inputCollectAction
    }

}


