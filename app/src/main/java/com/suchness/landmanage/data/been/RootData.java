package com.suchness.landmanage.data.been;

import com.chad.library.adapter.base.entity.node.BaseExpandNode;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.entity.node.NodeFooterImp;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @Author hejunfeng
 * @Date 15:35 2021/3/26 0026
 * @Description com.analysis.wisdomtraffic.been
 **/
public class RootData extends BaseExpandNode implements NodeFooterImp {
    private List<BaseNode> childData;
    private String title;

    public RootData(List<BaseNode> childData, String title) {
        this.childData = childData;
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return childData;
    }

    @Nullable
    @Override
    public BaseNode getFooterNode() {
        return null;
    }
}
