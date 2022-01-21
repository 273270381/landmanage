package com.suchness.landmanage.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.suchness.deeplearningapp.app.base.BaseFragment;
import com.suchness.landmanage.R;
import com.suchness.landmanage.app.utils.AppOperator;
import com.suchness.landmanage.app.utils.DataUtils;
import com.suchness.landmanage.databinding.FragmentPreviewPicBinding;
import com.suchness.landmanage.ui.adapter.PrewAdapter;
import com.suchness.landmanage.viewmodel.HomeViewModel;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_IMAGE_PATH;
import static com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_VIDEO_PATH;

/**
 * @author: hejunfeng
 * @date: 2021/12/27 0027
 */
public class PreviewFileFragment extends BaseFragment<HomeViewModel, FragmentPreviewPicBinding> {
    private ViewPager2 viewPager;
    private int position;
    private List<String> urlList;
    private ImageButton share;
    private String cameraName;
    private Boolean isVideo;
    private String url;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 0 :
                    position = msg.arg1;
                    urlList = (List<String>) msg.obj;
                    setAdapter();
                    break;
            }
        }
    };


    @Override
    public int layoutId() {
        return R.layout.fragment_preview_pic;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        assert bundle != null;
        isVideo = bundle.getBoolean("isVideo");
        cameraName = bundle.getString("cameraname");
        url = bundle.getString("url");
        getData(url);

    }

    private void setAdapter() {
        PrewAdapter adapter = new PrewAdapter(getActivity(),urlList);
        viewPager = mDatabind.getRoot().findViewById(R.id.viewpager2);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                url = urlList.get(position);
            }
        });
        share = mDatabind.getRoot().findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImg("分享","分享","分享", url);
            }
        });
    }

    private void getData(String s) {
        AppOperator.runOnThread(new Runnable() {
            @Override
            public void run() {
                String path = "";
                if (isVideo){
                    path = DEFAULT_SAVE_VIDEO_PATH+cameraName;
                }else{
                    path = DEFAULT_SAVE_IMAGE_PATH+cameraName;
                }
                List<String> urls = DataUtils.getImagePathFromSD(path);
                int position = urls.lastIndexOf(s);
                Message msg = Message.obtain();
                msg.what = 0;
                msg.arg1 = position;
                msg.obj = urls;
                handler.sendMessage(msg);
            }
        });
    }

    private void shareImg(String dlgTitle, String subject, String content, String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        if (url != null && !"".equals(url)){
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));
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
