package com.suchness.landmanage.ui.adapter;


import com.chad.library.adapter.base.BaseNodeAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.suchness.landmanage.data.been.ItemData;
import com.suchness.landmanage.data.been.RootData;
import com.suchness.landmanage.ui.adapter.provider.RootNodeProvider;
import com.suchness.landmanage.ui.adapter.provider.SecondNodeProvider;

import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * @Author hejunfeng
 * @Date 15:05 2021/3/26 0026
 * @Description com.analysis.wisdomtraffic.adapter
 **/
public class PicNodeAdapter extends BaseNodeAdapter {

    public PicNodeAdapter() {
        super();
    }

    public void setNodeProvider(SecondNodeProvider secondNodeProvider){
        addFullSpanNodeProvider(new RootNodeProvider());
        addNodeProvider(secondNodeProvider);
    }

    @Override
    protected int getItemType(@NotNull List<? extends BaseNode> list, int i) {
        BaseNode node = list.get(i);
        if (node instanceof RootData){
            return 0;
        }else if(node instanceof ItemData){
            return 1;
        }
        return -1;
    }
}
