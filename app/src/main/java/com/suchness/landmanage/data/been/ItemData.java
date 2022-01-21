package com.suchness.landmanage.data.been;

import com.chad.library.adapter.base.entity.node.BaseNode;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @Author hejunfeng
 * @Date 15:39 2021/3/26 0026
 * @Description com.analysis.wisdomtraffic.been
 **/
public class ItemData extends BaseNode {
    private String path;

    public ItemData(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return null;
    }
}
