package com.suchness.landmanage.ui.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.suchness.landmanage.R;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * @author: hejunfeng
 * @date: 2021/12/24 0024
 */
public class ImageAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public ImageAdapter(int layoutResId, List<String> urls) {
        super(layoutResId,urls);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, String s) {
        Glide.with(getContext()).load(s).apply(RequestOptions.bitmapTransform( new RoundedCorners(20)))
                .transition(DrawableTransitionOptions.withCrossFade(500)).into((ImageView) baseViewHolder.getView(R.id.img));
    }
}
