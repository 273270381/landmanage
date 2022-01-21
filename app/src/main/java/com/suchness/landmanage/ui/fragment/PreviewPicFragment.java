package com.suchness.landmanage.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.viewpager2.widget.ViewPager2;
import com.suchness.deeplearningapp.app.base.BaseFragment;
import com.suchness.landmanage.R;
import com.suchness.landmanage.databinding.FragmentPreviewPicBinding;
import com.suchness.landmanage.ui.adapter.PrewAdapter;
import com.suchness.landmanage.viewmodel.HomeViewModel;
import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * @author: hejunfeng
 * @date: 2021/12/25 0025
 */
public class PreviewPicFragment extends BaseFragment<HomeViewModel, FragmentPreviewPicBinding> {
    private ViewPager2 viewPager;
    private int position;
    private List<String> urls;
    private ImageButton share;
    private String url;


    @Override
    public int layoutId() {
        return R.layout.fragment_preview_pic;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        assert bundle != null;
        position = bundle.getInt("position");
        urls = (List<String>) bundle.getSerializable("urls");
        PrewAdapter adapter = new PrewAdapter(getActivity(),urls);
        viewPager = mDatabind.getRoot().findViewById(R.id.viewpager2);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                url = urls.get(position);
            }
        });
        share = mDatabind.getRoot().findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImg("分享",url,url,null);
            }
        });
    }

    private void shareImg(String dlgTitle, String subject, String content, String uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        if (uri != null && !"".equals(uri)){
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }
        if (subject != null && !"".equals(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (content != null && !"".equals(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }
        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            startActivity(intent);
        }
    }
}
