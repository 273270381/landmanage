package com.suchness.landmanage.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.suchness.deeplearningapp.app.base.BaseFragment;
import com.suchness.landmanage.R;
import com.suchness.landmanage.app.utils.AnimationUtils;
import com.suchness.landmanage.app.utils.AppOperator;
import com.suchness.landmanage.app.utils.DataUtils;
import com.suchness.landmanage.app.utils.DensityUtil;
import com.suchness.landmanage.app.utils.ToastNotRepeat;
import com.suchness.landmanage.app.weight.ScrollICheckView;
import com.suchness.landmanage.data.been.ItemData;
import com.suchness.landmanage.data.been.RootData;
import com.suchness.landmanage.databinding.FragmentFileChildBinding;
import com.suchness.landmanage.ui.adapter.PicNodeAdapter;
import com.suchness.landmanage.ui.adapter.provider.SecondNodeProvider;
import com.suchness.landmanage.ui.adapter.provider.LongClickCallBack;
import com.suchness.landmanage.viewmodel.HomeViewModel;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import me.hgj.jetpackmvvm.demo.app.weight.loadCallBack.EmptyCallback;
import me.hgj.jetpackmvvm.demo.app.weight.loadCallBack.LoadingCallback;
import static com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_IMAGE_PATH;
import static com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_VIDEO_PATH;

/**
 * @author: hejunfeng
 * @date: 2021/12/25 0025
 */
public class FileChildFragment extends BaseFragment<HomeViewModel, FragmentFileChildBinding> {
    private PicNodeAdapter adapter;
    private String cameraName;
    private String excPath;
    private SecondNodeProvider secondNodeProvider;
    private boolean isShowCheck = false;
    private List<String> checkList = new ArrayList<>();
    private int height;
    private String cid = "";
    private LoadService<Object> loadsir;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 0:
                    List<RootData> datas = (List<RootData>) msg.obj;
                    if (datas.size() == 0){
                        loadsir.showCallback(EmptyCallback.class);
                    }else{
                        loadsir.showSuccess();
                    }
                    adapter.setList(datas);
                    //mDatabind.recyclerView.scheduleLayoutAnimation();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public int layoutId() {
        return R.layout.fragment_file_child;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        cid = bundle.getString("cid");
        cameraName = bundle.getString("name");
        height = DensityUtil.dip2px(getActivity(),81);
        mDatabind.footLayout.scrollTo(0,-height);
        secondNodeProvider = new SecondNodeProvider(FileChildFragment.this,cameraName);
        mDatabind.recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
        adapter = new PicNodeAdapter();
        adapter.setNodeProvider(secondNodeProvider);
        mDatabind.recyclerView.setAdapter(adapter);

        mDatabind.footLayout.setShareClickListener(new ScrollICheckView.ScrollOnClickListener() {
            @Override
            public void click() {
                checkList = secondNodeProvider.getMap();
                senfiles("分享",checkList);
            }
        });

        mDatabind.footLayout.setDeleteClickListener(new ScrollICheckView.ScrollOnClickListener() {
            @Override
            public void click() {
                checkList = secondNodeProvider.getMap();
                deleteFile(checkList);
            }
        });

        loadsir = LoadSir.getDefault().register(mDatabind.recyclerView, new Callback.OnReloadListener() {
            @Override
            public void onReload(View view) {
                getData(excPath);
            }
        });
    }

    @Override
    public void lazyLoadData() {
        getData(excPath);
    }


    @Override
    public void initData() {
        if (cid.equals("图片")){
            excPath = DEFAULT_SAVE_IMAGE_PATH+cameraName;
        }else{
            excPath =  DEFAULT_SAVE_VIDEO_PATH+cameraName;
        }
        secondNodeProvider.setLongClickCallBack(new LongClickCallBack() {

            @Override
            public void showCheckBox() {
                if (!isShowCheck){
                    secondNodeProvider.setShowCheckBox(true);
                    adapter.notifyDataSetChanged();
                }else{
                    secondNodeProvider.setShowCheckBox(false);
                    adapter.notifyDataSetChanged();
                }
                isShowCheck = !isShowCheck;
            }

            @Override
            public void addFooter() {
                if (isShowCheck){
                    mDatabind.footLayout.ScrollTo(0,height);
                    AnimationUtils.startZoomAnim(mDatabind.recyclerView,mDatabind.recyclerView.getHeight()-height);
                }else{
                    mDatabind.footLayout.ScrollTo(0,-height);
                    AnimationUtils.startZoomAnim(mDatabind.recyclerView,mDatabind.recyclerView.getHeight()+height);
                }
                checkList.clear();
                secondNodeProvider.clearMap();

            }
        });
    }

    /**
     * @return java.util.List
     * @Author hejunfeng
     * @Date 14:37 2021/3/26 0026
     * @Param [list]
     * @Description 去掉list重复元素
     **/
    public static List removeDuplicateWithOrder(List list) {
        Set set = new HashSet();
        List newList = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (set.add(element))
                newList.add(element);
        }
        return newList;
    }

    public void getData(String path){
        loadsir.showCallback(LoadingCallback.class);
        AppOperator.runOnThread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                List<String> pathList = DataUtils.getImagePathFromSD(path);
                List<String> dateList = removeDuplicateWithOrder(DataUtils.getFileTime(pathList));
                List<BaseNode> list = new ArrayList<>();
                for (String dataStr : dateList){
                    List<BaseNode> items = new ArrayList<>();
                    for (String pathStr : pathList){
                        long time = new File(pathStr).lastModified();
                        String date = format.format(time);
                        if (dataStr.equals(date)){
                            ItemData itemData = new ItemData(pathStr);
                            items.add(itemData);
                        }
                    }
                    RootData rootData = new RootData(items,dataStr);
                    list.add(rootData);
                }
                Message msg = Message.obtain();
                msg.what = 0;
                msg.obj = list;
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * 文件发送
     * @param dlgTitle
     * @param urls
     */
    private void senfiles( String dlgTitle, List<String> urls) {
        ArrayList<Uri> files = new ArrayList<>();
        if (urls.size() > 0){
            for (String url : urls){
                Uri uri = Uri.parse(url);
                files.add(uri);
            }
            if (files.size() == 0) {
                return;
            }
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            intent.setType("*/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // 设置弹出框标题
            // 自定义标题
            if (dlgTitle != null && !"".equals(dlgTitle)) {
                startActivity(Intent.createChooser(intent, dlgTitle));
            } else { // 系统默认标题
                startActivity(intent);
            }
        }else{
            ToastNotRepeat.show(getActivity(),"请选择文件");
        }
    }

    private void deleteFile(List<String> urls){
        if (urls.size() > 0){
            for (String url : urls){
                File file = new File(url);
                if (file.exists() && file.isFile()) {
                    file.delete();
                }
            }
            isShowCheck = false;
            secondNodeProvider.setShowCheckBox(false);
            mDatabind.footLayout.ScrollTo(0,-height);
            AnimationUtils.startZoomAnim(mDatabind.recyclerView,mDatabind.recyclerView.getHeight()+height);
            ToastNotRepeat.show(getActivity(),"删除成功");
            getData(excPath);
            adapter.notifyDataSetChanged();
        }else{
            ToastNotRepeat.show(getActivity(),"请选择文件");
        }
    }

    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    public static FileChildFragment newInstance(String cid,String fileName){
        Bundle bundle = new Bundle();
        bundle.putString("cid",cid);
        bundle.putString("name",fileName);
        FileChildFragment fragment = new FileChildFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
