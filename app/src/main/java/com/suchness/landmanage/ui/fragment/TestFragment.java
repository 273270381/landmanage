package com.suchness.landmanage.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.suchness.deeplearningapp.app.base.BaseFragment;
import com.suchness.landmanage.R;
import com.suchness.landmanage.databinding.FragmentTestBindingImpl;
import com.suchness.landmanage.viewmodel.HomeViewModel;
import com.videogo.widget.TitleBar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author: hejunfeng
 * @date: 2021/12/21 0021
 */
public class TestFragment extends BaseFragment<HomeViewModel, FragmentTestBindingImpl> {


    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public int layoutId() {
        return R.layout.fragment_test;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        TitleBar mPortraitTitleBar = mDatabind.getRoot().findViewById(R.id.title_bar_portrait);
        mPortraitTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff),getResources().getDrawable(R.color.colorPrimary),
                null);
    }
}
