package com.suchness.landmanage.ui.adapter

import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.suchness.deeplearningapp.app.weight.customview.CollectView
import com.suchness.landmanage.R
import com.suchness.landmanage.app.ext.setAdapterAnimation
import com.suchness.landmanage.app.utils.SettingUtil
import com.suchness.landmanage.data.been.cityInfo
import com.suchness.landmanage.data.been.deviceInfo
import com.videogo.device.DeviceInfo

/**
 * @author: hejunfeng
 * @date: 2021/12/25 0025
 */
class FileAdapter(data: MutableList<String>): BaseDelegateMultiAdapter<String, BaseViewHolder>(data) {

    private var collectAction: (item: String, v: CollectView, position: Int) -> Unit =
        { _: String, _: CollectView, _: Int -> }


    init {
        setAdapterAnimation(SettingUtil.getListMode())
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<String>() {
            override fun getItemType(data: List<String>, position: Int): Int {
                //根据是否有图片 判断为文章还是项目，好像有点low的感觉。。。我看实体类好像没有相关的字段，就用了这个，也有可能是我没发现
                //return if (TextUtils.isEmpty(data[position].envelopePic)) Ariticle else Project
                return 1
            }
        })
        // 第二步，绑定 item 类型
        getMultiTypeDelegate()?.let {
            it.addItemType(1, R.layout.item_file)
        }
    }

    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.devDesc,item)
    }
}