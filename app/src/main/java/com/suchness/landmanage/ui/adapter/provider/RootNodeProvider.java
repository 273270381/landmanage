package com.suchness.landmanage.ui.adapter.provider;

import android.view.View;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.suchness.landmanage.R;
import com.suchness.landmanage.data.been.RootData;

import org.jetbrains.annotations.NotNull;

/**
 * @Author hejunfeng
 * @Date 15:08 2021/3/26 0026
 * @Description com.analysis.wisdomtraffic.ui.filemanage.provider
 **/
public class RootNodeProvider extends BaseNodeProvider {
    @Override
    public int getItemViewType() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.def_section_head;
    }

    @Override
    public void convert(@NotNull BaseViewHolder baseViewHolder, BaseNode baseNode) {
        RootData data = (RootData)baseNode;
        baseViewHolder.setText(R.id.header,data.getTitle());
    }

    @Override
    public void onClick(@NotNull BaseViewHolder helper, @NotNull View view, BaseNode data, int position) {
        getAdapter().expandOrCollapse(position);
    }
}
