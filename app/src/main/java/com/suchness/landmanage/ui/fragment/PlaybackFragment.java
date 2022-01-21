package com.suchness.landmanage.ui.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.suchness.deeplearningapp.app.base.BaseFragment;
import com.suchness.landmanage.R;
import com.suchness.landmanage.app.App;
import com.suchness.landmanage.app.utils.AudioPlayUtil;
import com.suchness.landmanage.app.utils.EZUtils;
import com.suchness.landmanage.app.utils.RemoteListUtil;
import com.suchness.landmanage.app.utils.VerifyCodeInput;
import com.suchness.landmanage.app.weight.loadtextview.LoadingTextView;
import com.suchness.landmanage.app.weight.loadtextview.LoadingView;
import com.suchness.landmanage.app.weight.playback.ClickedListItem;
import com.suchness.landmanage.app.weight.playback.CloudPartInfoFileEx;
import com.suchness.landmanage.app.weight.playback.QueryPlayBackCloudListAsyncTask;
import com.suchness.landmanage.app.weight.playback.QueryPlayBackListTaskCallback;
import com.suchness.landmanage.app.weight.playback.QueryPlayBackLocalListAsyncTask;
import com.suchness.landmanage.app.weight.playback.RemoteListContant;
import com.suchness.landmanage.app.weight.playback.WaitDialog;
import com.suchness.landmanage.app.weight.realplay.ScreenOrientationHelper;
import com.suchness.landmanage.data.been.db.DBManager;
import com.suchness.landmanage.data.been.db.PicFilePath;
import com.suchness.landmanage.data.been.db.Verifcode;
import com.suchness.landmanage.data.been.db.VideoFilePath;
import com.suchness.landmanage.databinding.FragmentPlayBackBindingImpl;
import com.suchness.landmanage.databinding.FragmentRealPlayBindingImpl;
import com.suchness.landmanage.ui.adapter.SectionListAdapter;
import com.suchness.landmanage.ui.adapter.StandardArrayAdapter;
import com.suchness.landmanage.viewmodel.HomeViewModel;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.device.DeviceInfoEx;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.InnerException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.EZDeviceRecordFile;
import com.videogo.openapi.bean.resp.CloudFile;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.remoteplayback.RemoteFileInfo;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.DateTimeUtil;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.MediaScanner;
import com.videogo.util.RotateViewUtil;
import com.videogo.util.SDCardUtil;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.CustomRect;
import com.videogo.widget.CustomTouchListener;
import com.videogo.widget.PinnedHeaderListView;
import com.videogo.widget.TitleBar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_IMAGE_PATH;
import static com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_VIDEO_PATH;
import static com.suchness.landmanage.app.ext.LoadingDialogExtKt.getOpenSDK;
import static com.videogo.util.Utils.showToast;

/**
 * @author: hejunfeng
 * @date: 2021/12/21 0021
 */
public class PlaybackFragment extends BaseFragment<HomeViewModel, FragmentPlayBackBindingImpl> implements QueryPlayBackListTaskCallback,
        SectionListAdapter.OnHikItemClickListener, SurfaceHolder.Callback, View.OnClickListener, View.OnTouchListener,
        StandardArrayAdapter.ArrayAdapterChangeListener, VerifyCodeInput.VerifyCodeInputListener {
    // 动画更新
    private static final int ANIMATION_UPDATE = 0xde;
    private static final String TAG = "PlayBackListActivity";
    // 选择日期请求码
    // 选择日期请求码
    private static final int REQUEST_CADENLAR = 0x56;
    // 显示数据网络提示
    private boolean mShowNetworkTip = true;
    private BroadcastReceiver mReceiver = null;
    //验证码
    private String mVerifyCode ;
    // 查询时间
    private Date queryDate = null;
    // 自定义ListView
    private PinnedHeaderListView pinnedHeaderListView;
    private PinnedHeaderListView mPinnedHeaderListViewForLocal;
    private RotateViewUtil mRecordRotateViewUtil = null;
    // 列表适配器
    private StandardArrayAdapter arrayAdapter;
    // ListView适配器
    private SectionListAdapter sectionAdapter;
    private StandardArrayAdapter mArrayAdapterForLocal;
    // ListView适配器
    private SectionListAdapter mSectionAdapterForLocal;
    // 列表查询task(云存储)
    private QueryPlayBackCloudListAsyncTask queryPlayBackCloudListAsyncTask;
    // 列表查询task(本地)
    private QueryPlayBackLocalListAsyncTask queryPlayBackLocalListAsyncTask;
    // 标题
    private TitleBar mTitleBar;
    // 查询异常布局
    private LinearLayout queryExceptionLayout;
    // 没有数据
    private LinearLayout novideoImg;
    // 没有数据本地
    private LinearLayout mNoVideoImgLocal;
    // 加载进度圈
    private LoadingTextView loadingBar;
    // 播放缓冲百分比
    private TextView remoteLoadingBufferTv, touchLoadingBufferTv;
    // 播放区域
    private RelativeLayout remotePlayBackArea;
    // 结束时间文本
    private TextView endTimeTV = null;
    // 关闭播放区域按钮
    private ImageButton exitBtn;
    private SurfaceView surfaceView = null;
    private TextView mRemotePlayBackRatioTv = null;

    private CustomTouchListener mRemotePlayBackTouchListener = null;
    // 播放比例
    private float mPlayScale = 1;
    // 本地信息
    private LocalInfo localInfo = null;
    // 音频播放
    private AudioPlayUtil mAudioPlayUtil = null;
    // 存放上一次的流量 */
    private long mStreamFlow = 0;
    // 当前流量 */
    private int mRealFlow = 0;

    // 播放进度条
    private SeekBar progressSeekbar = null;
    private ProgressBar progressBar = null;
    // 开始时间文本
    private TextView beginTimeTV = null;

    // 当前被点击的item
    private ClickedListItem currentClickItemFile;
    // 本地播放文件
    private RemoteFileInfo fileInfo;
    // 云端播放文件
    private CloudFile currentCloudFile;
    // 播放分辨率
    private float mRealRatio = Constant.LIVE_VIEW_RATIO;
    // 播放状态
    private int status = RemoteListContant.STATUS_INIT;
    // 是否显示播放控制区，默认为没有显示
    private boolean notShowControlArea = true;
    // 播放控制区域
    private LinearLayout controlArea = null;
    private LinearLayout progressArea = null;
    // 拍照
    private ImageButton captureBtn = null;
    // 录像
    private ImageButton videoRecordingBtn = null;
    // 停止录像
    private ImageButton videoRecordingBtn_end = null;
    private View mRealPlayRecordContainer = null;
    // 下载按钮
    private LinearLayout downloadBtn = null;
    // Loading图片
    private LoadingView loadingImgView;
    private LinearLayout loadingPbLayout;
    // 流量统计
    private TextView flowTV = null;
    private float startX, startY, endX, endY;
    // 屏幕方向
    private int mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    // 页面Layout
    private RelativeLayout remoteListPage = null;
    // 错误信息显示
    private TextView errorInfoTV;
    // 错误重播按钮
    private ImageButton errorReplay;
    // loading时停止出现的播放按钮
    private ImageButton loadingPlayBtn;
    // 暂停/播放按钮
    private ImageButton pauseBtn = null;
    // 声音按钮
    private ImageButton soundBtn = null;
    // 是否暂停播放，默认为没有暂停
    private boolean notPause = true;
    // 重播和下一个播放 控制区域
    private LinearLayout replayAndNextArea = null;
    private Rect mRemotePlayBackRect = null;
    private LinearLayout mRemotePlayBackRecordLy = null;
    private int mCaptureDisplaySec = 0;
    // 录像时间
    private int mRecordSecond = 0;
    // 控制栏时间值
    private int mControlDisplaySec = 0;
    // 流量限定提示框
    private AlertDialog mLimitFlowDialog = null;
    private int mCountDown = 10;
    // 录像标记点
    private ImageView mRemotePlayBackRecordIv = null;
    // 播放时间
    private TextView mRemotePlayBackRecordTv = null;
    // 重播按钮
    private ImageButton replayBtn;
    // 下一个播放按钮
    private ImageButton nextPlayBtn;
    // 密码输入Dialog
    private Dialog safeDialog;
    // 输入法管理类
    private InputMethodManager imm;
    // 定时器
    private Timer mUpdateTimer = null;
    // 定时器执行的任务
    private TimerTask mUpdateTimerTask = null;
    private String mRecordTime = null;
    // 是否为选择日期事件
    private boolean isDateSelected = false;
    // 下载动画
    private ImageView downloading;
    // 下载个数
    private TextView downloadingNumber;
    // 下载区域布局
    private RelativeLayout downLayout;
    // 下载popup
    private PopupWindow downPopup;
    // 云播放下载提示状态
    private boolean isCloudPrompt = false;
    // 云播放下载提示状态key
    private static final String HAS_BEAN_CLOUD_PROMPT = "has_bean_cloud_prompt";
    private SharedPreferences sharedPreferences;
    // 抖动动画
    private Animation downShake;
    private AnimationDrawable downDrawable;
    // 是否第一次弹出非wifi下载提示 默认true
    private boolean isFirstWifiDialog = true;
    private ImageView matteImage;
    /*** 自动播放 *************/
    private LinearLayout autoLayout;
    private TextView textTime;
    // 取消按钮
    private Button cancelBtn;
    // 文件大小文本
    private TextView fileSizeText;
    // 标题栏中间日期边上的向下箭头
    private ImageView selDateImage;
    /********************/
    // 进度条拖动时的进度圈
    private LinearLayout touchProgressLayout;
    // 全屏按钮
    private CheckTextButton mFullscreenButton;
    private ScreenOrientationHelper mScreenOrientationHelper;
    private WaitDialog mWaitDlg = null;
    // 右上角编辑按钮
    private TextView rightEditView;
    // 左上角返回按钮
    private Button backBtn;
    // 删除视频
    private TextView deleteVideoText;
    private EZPlayer mPlayer = null;
    private String mCameraId = "";
    private RelativeLayout mContentTabCloudRl;
    private RelativeLayout mContentTabDeviceRl;
    private CheckTextButton mCheckBtnCloud;
    private CheckTextButton mCheckBtnDevice;
    private FrameLayout mTabContentMainFrame;
    private boolean mIsLocalDataQueryPerformed = false;
    // whether it is in recording
    private boolean  bIsRecording = false;
    private RelativeLayout mControlBarRL;
    private TitleBar mLandscapeTitleBar = null;
    private CheckTextButton mFullScreenTitleBarBackBtn;
    private EZCameraInfo mCameraInfo = null;
    private DeviceInfoEx mDeviceInfoEx;
    private EZDeviceRecordFile mDeviceRecordInfo = null;
    private EZCloudRecordFile mCloudRecordInfo = null;
    private AlertDialog mAlertDialog;
    private Handler playBackHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                // 片段播放完毕
                // 380061即开始时间>=结束时间，播放完成
                case ErrorCode.ERROR_CAS_RECORD_SEARCH_START_TIME_ERROR:
                    Log.d(TAG,"ERROR_CAS_RECORD_SEARCH_START_TIME_ERROR");
                    handlePlaySegmentOver();
                    break;
                case EZConstants.EZPlaybackConstants.MSG_REMOTEPLAYBACK_PLAY_FINISH:
                    Log.d(TAG,"MSG_REMOTEPLAYBACK_PLAY_FINISH");
                    handlePlaySegmentOver();
                    break;
                // 画面显示第一帧
                case EZConstants.EZPlaybackConstants.MSG_REMOTEPLAYBACK_PLAY_SUCCUSS:
                    handleFirstFrame(msg);
                    break;
                case EZConstants.EZPlaybackConstants.MSG_REMOTEPLAYBACK_STOP_SUCCESS:
                    handleStopPlayback();
                    break;
                case EZConstants.EZPlaybackConstants.MSG_REMOTEPLAYBACK_PLAY_FAIL:
                    ErrorInfo errorInfo = (ErrorInfo)msg.obj;
                    handlePlayFail(errorInfo);
                    break;
                // 处理播放链接异常
                case RemoteListContant.MSG_REMOTELIST_CONNECTION_EXCEPTION:
                    if (msg.arg1 == ErrorCode.ERROR_CAS_RECORD_SEARCH_START_TIME_ERROR) {
                        handlePlaySegmentOver();
                    }
                    break;
                case RemoteListContant.MSG_REMOTELIST_UI_UPDATE:
                    updateRemotePlayUI();
                    break;
                case RemoteListContant.MSG_REMOTELIST_STREAM_TIMEOUT:
                    break;
                default:
                    break;
            }
        }

    };

    // 重播
    private void reConnectPlay(int type, Calendar uiPlayTimeOnStop) {
        newPlayInit(false, false);
        if (type == RemoteListContant.TYPE_CLOUD) {
        } else {
            RemoteFileInfo fileInfo1 = this.fileInfo.copy();
            fileInfo1.setStartTime(uiPlayTimeOnStop);
        }
    }

    private void updateRemotePlayUI() {
        if (mControlDisplaySec == 5) {
            mControlDisplaySec = 0;
            if (status != RemoteListContant.STATUS_INIT) {
                hideControlArea();
            }
        }
        if (mLimitFlowDialog != null && mLimitFlowDialog.isShowing()) {
            if (mCountDown == 0) {
                dismissPopDialog(mLimitFlowDialog);
                mLimitFlowDialog = null;
                // 流量大于限定时，停止播放
                if (status != RemoteListContant.STATUS_STOP) {
                    onPlayExitBtnOnClick();
                }
            }
        }
        if (bIsRecording) {
            updateRecordTime();
        }

        if (mPlayer != null && status == RemoteListContant.STATUS_PLAYING) {
            Calendar osd = mPlayer.getOSDTime();
            if(osd != null) {
                handlePlayProgress(osd);
            }
        }
    }

    // 退出播放按钮事件处理
    private void onPlayExitBtnOnClick() {
        stopRemoteListPlayer();
        remotePlayBackArea.setVisibility(View.GONE);
        // 不允许旋转屏幕
        mScreenOrientationHelper.disableSensorOrientation();
        controlArea.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        mControlDisplaySec = 0;
        loadingImgView.setVisibility(View.GONE);
        loadingPbLayout.setVisibility(View.GONE);
        touchProgressLayout.setVisibility(View.GONE);
        status = RemoteListContant.STATUS_STOP;
        notShowControlArea = true;
        notPause = false;
        pinnedHeaderListView.startAnimation();
    }

    // 更新录像时间
    private void updateRecordTime() {
        if (mRemotePlayBackRecordIv.getVisibility() == View.VISIBLE) {
            mRemotePlayBackRecordIv.setVisibility(View.INVISIBLE);
        } else {
            mRemotePlayBackRecordIv.setVisibility(View.VISIBLE);
        }
        // 计算分秒
        int leftSecond = mRecordSecond % 3600;
        int minitue = leftSecond / 60;
        int second = leftSecond % 60;
        // 显示录像时间
        String recordTime = String.format("%02d:%02d", minitue, second);
        mRemotePlayBackRecordTv.setText(recordTime);
    }

    // 更新流量统计
    @SuppressLint("DefaultLocale")
    private void updateRemotePlayBackFlowTv(long streamFlow) {}

    private void dismissPopDialog(AlertDialog popDialog) {
        if (popDialog != null && popDialog.isShowing() ) {
            try {
                popDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handlePlaySegmentOver() {
        LogUtil.e(TAG, "handlePlaySegmentOver");
        stopRemoteListPlayer();
        stopRemotePlayBackRecord();

        if (mOrientation != Configuration.ORIENTATION_PORTRAIT) {
            setRemoteListSvLayout();
        }
        controlArea.setVisibility(View.GONE);
        mControlDisplaySec = 0;
        exitBtn.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        beginTimeTV.setText(endTimeTV.getText());
        notShowControlArea = true;
        status = RemoteListContant.STATUS_STOP;
        loadingPbLayout.setVisibility(View.VISIBLE);
        autoLayout.setVisibility(View.GONE);
    }

    private void timeBucketUIInit(long beginTime, long endTime) {
        int diffSeconds = (int) (endTime - beginTime) / 1000;
        String convToUIDuration = RemoteListUtil.convToUIDuration(diffSeconds);
        beginTimeTV.setText(RemoteListContant.VIDEO_DUAR_BEGIN_INIT);
        endTimeTV.setText(convToUIDuration);
    }

    // 停止播放
    private void stopRemotePlayBackRecord() {
        if (!bIsRecording) {
            return;
        }
        mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
        showToast(getContext(),getResources().getString(R.string.already_saved_to_volume));

        if(mPlayer != null) {
            mPlayer.stopLocalRecord();
        }
        // 计时按钮不可见
        mRemotePlayBackRecordLy.setVisibility(View.GONE);
        // 设置录像按钮为check状态
        mRecordRotateViewUtil.applyRotation(mRealPlayRecordContainer, videoRecordingBtn_end,
                videoRecordingBtn, 0, 90);
        mCaptureDisplaySec = 0;
    }


    @Override
    public int layoutId() {
        return R.layout.fragment_play_back;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        imm = (InputMethodManager)getContext(). getSystemService(Context.INPUT_METHOD_SERVICE);
        // 保持屏幕常亮
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWaitDlg = new WaitDialog(getContext(), android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDlg.setCancelable(false);
        getData();
        if (mCameraInfo ==null){
            LogUtil.d(TAG,"cameraInfo is null");
        }
        initUi();
        onQueryExceptionLayoutTouched();
        initListener();
        initRemoteListPlayer();
        fakePerformClickUI();
    }

    private void fakePerformClickUI() {
        if (autoLayout.getVisibility() == View.VISIBLE) {
            autoLayout.setVisibility(View.GONE);
        }
        fileSizeText.setText("");
        downloadBtn.setPadding(0, 0, 0, 0);
        remotePlayBackArea.setVisibility(View.VISIBLE);
        errorReplay.setVisibility(View.GONE);
        loadingPlayBtn.setVisibility(View.GONE);
        hideControlArea();
    }

    private void showTab(int id) {
        switch (id) {
            case R.id.novideo_img:
                novideoImg.setVisibility(View.VISIBLE);
                loadingBar.setVisibility(View.GONE);
                mTabContentMainFrame.setVisibility(View.VISIBLE);
                break;
            case R.id.novideo_img_device:
                mNoVideoImgLocal.setVisibility(View.VISIBLE);
                mPinnedHeaderListViewForLocal.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                mTabContentMainFrame.setVisibility(View.VISIBLE);
                break;
            case R.id.loadingTextView:
                novideoImg.setVisibility(View.GONE);
                loadingBar.setVisibility(View.VISIBLE);
                mTabContentMainFrame.setVisibility(View.GONE);
                break;
            case R.id.content_tab_device_root:
                mNoVideoImgLocal.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                mTabContentMainFrame.setVisibility(View.VISIBLE);
                break;
            case R.id.ez_tab_content_frame:
                novideoImg.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                mTabContentMainFrame.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
    private Calendar getTimeBarSeekTime() {
        if (currentClickItemFile != null) {
            long beginTime = currentClickItemFile.getBeginTime();
            long endTime = currentClickItemFile.getEndTime();
            int progress = progressSeekbar.getProgress();
            long seekTime = (((endTime - beginTime) * progress) / RemoteListContant.PROGRESS_MAX_VALUE) + beginTime;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(seekTime);
            return c;
        }
        return null;
    }

    // 播放失败处理
    private void handlePlayFail(ErrorInfo errorInfo) {
        LogUtil.d(TAG, "handlePlayFail. Playback failed. error info is " + errorInfo.toString());
        status = RemoteListContant.STATUS_STOP;
        stopRemoteListPlayer();
        int errorCode = errorInfo.errorCode;
        switch (errorCode) {
            case ErrorCode.ERROR_TRANSF_ACCESSTOKEN_ERROR:{
            }
            // 收到这两个错误码，可以弹出对话框，让用户输入密码后，重新取流预览
            case ErrorCode.ERROR_INNER_VERIFYCODE_NEED:
            case ErrorCode.ERROR_INNER_VERIFYCODE_ERROR:{
                showTipDialog("");
                VerifyCodeInput.VerifyCodeInputDialog(getContext(),this).show();
            }
            break;
            default: {
                String txt = null;
                if (errorCode == ErrorCode.ERROR_CAS_CONNECT_FAILED) {
                    txt = getString(R.string.remoteplayback_connect_server_error);
                } else if (errorCode == 2004/*VideoGoNetSDKException.VIDEOGONETSDK_DEVICE_EXCEPTION*/) {
                    txt = getString(R.string.realplay_fail_connect_device);
                }  else if (errorCode == InnerException.INNER_DEVICE_NOT_EXIST) {
                    // 提示播放失败
                    txt = getString(R.string.camera_not_online);
                } else {
                    if (errorCode!=0){
                        //txt = getErrorTip(R.string.remoteplayback_fail, errorCode);
                        txt = errorInfo.description;
                    }
                }
                int errorId = 0; //getErrorId(errorCode);
                showTipDialog(errorId != 0 ? getString(errorId) : txt);
                if (errorCode == ErrorCode.ERROR_CAS_STREAM_RECV_ERROR
                        || errorCode == ErrorCode.ERROR_TRANSF_DEVICE_OFFLINE
                        || errorCode == ErrorCode.ERROR_CAS_PLATFORM_CLIENT_REQUEST_NO_PU_FOUNDED
                        || errorCode == ErrorCode.ERROR_CAS_MSG_PU_NO_RESOURCE) {
                }
            }
        }
    }

    private void showTipDialog(String txt) {
        loadingImgView.setVisibility(View.GONE);
        loadingPbLayout.setVisibility(View.GONE);
        touchProgressLayout.setVisibility(View.GONE);
        controlArea.setVisibility(View.GONE);
        mControlDisplaySec = 0;
        errorInfoTV.setVisibility(View.VISIBLE);
        errorReplay.setVisibility(View.VISIBLE);
        errorInfoTV.setText(txt);
    }

    private void handleFirstFrame(Message msg) {
        if (msg.arg1 != 0) {
            mRealRatio = (float) msg.arg2 / msg.arg1;
        }
        status = RemoteListContant.STATUS_PLAYING;
        notShowControlArea = true;
        controlArea.setVisibility(View.VISIBLE);
        progressArea.setVisibility(View.VISIBLE);
        mControlDisplaySec = 0;
        captureBtn.setEnabled(true);
        videoRecordingBtn.setEnabled(true);
        setRemoteListSvLayout();
        mScreenOrientationHelper.disableSensorOrientation();
        loadingImgView.setVisibility(View.GONE);
        loadingPbLayout.setVisibility(View.GONE);
        touchProgressLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        errorInfoTV.setVisibility(View.GONE);
        errorReplay.setVisibility(View.GONE);
        downloadBtn.setPadding(Utils.dip2px(getContext(), 5), 0, Utils.dip2px(getContext(), 5), 0);
        if (localInfo.isSoundOpen()) {
            // 打开声音
            if(mPlayer != null) {
                mPlayer.openSound();
            }
        } else {
            // 关闭声音
            if(mPlayer != null) {
                mPlayer.closeSound();
            }
        }

    }

    // 收到停止回放成功的消息后处理
    private void handleStopPlayback() {
        LogUtil.d(TAG, "stop playback success");
    }

    private void setRemoteListSvLayout() {
        // 设置播放窗口位置
        final int screenWidth = localInfo.getScreenWidth();
        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ? (localInfo.getScreenHeight() - localInfo
                .getNavigationBarHeight()) : localInfo.getScreenHeight();
        final RelativeLayout.LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation,
                localInfo.getScreenWidth(), (int) (localInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO), screenWidth,
                screenHeight);
        RelativeLayout.LayoutParams svLp = new RelativeLayout.LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        svLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        surfaceView.setLayoutParams(svLp);
        mRemotePlayBackTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);
        setPlayScaleUI(1, null, null);
    }

    private void setPlayScaleUI(float scale, CustomRect oRect, CustomRect curRect) {
        boolean bDisableZoom = true;
        if(bDisableZoom) {
            return;
        }
        if (scale == 1) {
            if (mPlayScale == scale) {
                return;
            }
            mRemotePlayBackRatioTv.setVisibility(View.GONE);
            try {
                if(mPlayer != null) {
                    mPlayer.setDisplayRegion(false, null, null);
                }
            } catch (BaseException e) {
                e.printStackTrace();
            }
        } else {
            if (mPlayScale == scale) {
                try {
                    if(mPlayer != null) {
                        mPlayer.setDisplayRegion(true, oRect, curRect);
                    }
                } catch (BaseException e) {
                    e.printStackTrace();
                }
                return;
            }
            RelativeLayout.LayoutParams realPlayRatioTvLp = (RelativeLayout.LayoutParams) mRemotePlayBackRatioTv
                    .getLayoutParams();
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                realPlayRatioTvLp.setMargins(Utils.dip2px(getContext(), 10), Utils.dip2px(getContext(), 10), 0, 0);
            } else {
                realPlayRatioTvLp.setMargins(Utils.dip2px(getContext(), 70), Utils.dip2px(getContext(), 20), 0, 0);
            }
            mRemotePlayBackRatioTv.setLayoutParams(realPlayRatioTvLp);
            String sacleStr = String.valueOf(scale);
            mRemotePlayBackRatioTv.setText(sacleStr.subSequence(0, Math.min(3, sacleStr.length())) + "X");
            mRemotePlayBackRatioTv.setVisibility(View.VISIBLE);
            notShowControlArea = false;
            onPlayAreaTouched();
            try {
                if(mPlayer != null) {
                    mPlayer.setDisplayRegion(true, oRect, curRect);
                }
            } catch (BaseException e) {
                e.printStackTrace();
            }
        }
        mPlayScale = scale;
    }

    private void onPlayAreaTouched() {
        if (status == RemoteListContant.STATUS_PLAYING || status == RemoteListContant.STATUS_PAUSE) {
            if (notShowControlArea) {
                showControlArea(true);
            } else {
                hideControlArea();
            }
        }
    }

    private void hideControlArea() {
        controlArea.setVisibility(View.GONE);
        mControlDisplaySec = 0;
        exitBtn.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        notShowControlArea = true;
        mLandscapeTitleBar.setVisibility(View.GONE);
    }

    // show: 1, visible 0, hide
    private void showControlArea(boolean show) {
        if(!show) {
            controlArea.setVisibility(View.GONE);
            return;
        }
        controlArea.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        mControlDisplaySec = 0;
        notShowControlArea = false;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            captureBtn.setVisibility(View.VISIBLE);
            videoRecordingBtn.setVisibility(View.VISIBLE);
        } else {
            exitBtn.setVisibility(View.GONE);
            captureBtn.setVisibility(View.VISIBLE);
            videoRecordingBtn.setVisibility(View.VISIBLE);
            mLandscapeTitleBar.setVisibility(View.VISIBLE);
        }
    }

    private void handlePlayProgress(Calendar osdTime) {
        long osd = osdTime.getTimeInMillis();
        long begin = currentClickItemFile.getBeginTime();
        long end = currentClickItemFile.getEndTime();
        double x = ((osd - begin) * RemoteListContant.PROGRESS_MAX_VALUE) / (double) (end - begin);
        int progress = (int) x;
        progressSeekbar.setProgress(progress);
        progressBar.setProgress(progress);
        int beginTimeClock = (int) ((osd - begin) / 1000);
        updateTimeBucketBeginTime(beginTimeClock);
    }
    private void updateTimeBucketBeginTime(int beginTimeClock) {
        String convToUIDuration = RemoteListUtil.convToUIDuration(beginTimeClock);
        beginTimeTV.setText(convToUIDuration);
    }

    private void initEZPlayer() {
        if(mPlayer != null) {
            // 停止录像
            mPlayer.stopLocalRecord();
            // 停止播放
            mPlayer.stopPlayback();
        } else {
            mPlayer = getOpenSDK().createPlayer(mCameraInfo.getDeviceSerial(),mCameraInfo.getCameraNo());
            String name = mCameraInfo.getDeviceSerial()+ String.valueOf(mCameraInfo.getCameraNo());
            List<Verifcode> verifcodeList =  DBManager.getInstance().get(Verifcode.class);
            if (verifcodeList.size() != 0){
                for (Verifcode verifcode : verifcodeList){
                    if (verifcode.getName().equals(name)){
                        mVerifyCode = verifcode.getCode();
                    }
                }
            }
            mPlayer.setPlayVerifyCode(mVerifyCode);
        }
    }
    private void initRemoteListPlayer() {
        stopRemoteListPlayer();

        if (status != RemoteListContant.STATUS_DECRYPT) {
            status = RemoteListContant.STATUS_INIT;
        }
    }

    private void initListener() {
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff),getResources().getDrawable(R.color.colorPrimary),
                getResources().getDrawable(R.drawable.message_back_selector_1));
//        backBtn = mTitleBar.addBackButton(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onExitCurrentPage();
//            }
//        });
        selDateImage = mTitleBar.addTitleButton(R.drawable.remote_cal_selector, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处于编辑状态不可点击
                if (sectionAdapter != null && sectionAdapter.isEdit()) {
                    return;
                }
                goToCalendar();
            }
        });
        mTitleBar.setOnTitleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 处于编辑状态不可点击
                if (sectionAdapter != null && sectionAdapter.isEdit()) {
                    return;
                }
                goToCalendar();
            }
        });

        downLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getWifiType() && isFirstWifiDialog) {
                    isFirstWifiDialog = false;
                } else {

                }
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onPlayExitBtnOnClick();
            }
        });

        rightEditView = new TextView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rightEditView.setLayoutParams(layoutParams);
        rightEditView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        rightEditView.setPadding(0, 0, Utils.dip2px(getContext(), 15), 0);
        mTitleBar.addRightView(rightEditView);
        rightEditView.setVisibility(View.GONE);
        rightEditView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                rightEditEvent();
            }
        });
        deleteVideoText.setOnClickListener(this);
        // loading继续播放按钮
        loadingPlayBtn.setOnClickListener(this);
        // 重播按钮事件
        replayBtn.setOnClickListener(this);
        errorReplay.setOnClickListener(this);
        // 播放下一片段按钮事件
        nextPlayBtn.setOnClickListener(this);
        // 查询异常区域touch事件
        queryExceptionLayout.setOnTouchListener(this);
        // 回放区域touch事件
        remotePlayBackArea.setOnTouchListener(this);
        // 控制区域touch事件
        controlArea.setOnTouchListener(this);
        controlArea.setOnClickListener(this);
        // 暂停播放按钮事件
        pauseBtn.setOnClickListener(this);
        // 声音按钮事件
        soundBtn.setOnClickListener(this);
        // 退出播放按钮事件
        exitBtn.setOnClickListener(this);
        // 抓图按钮事件
        captureBtn.setOnClickListener(this);
        // 录像按钮事件
        videoRecordingBtn.setOnClickListener(this);
        videoRecordingBtn_end.setOnClickListener(this);
        // 抓图/录像形成图片区域点击事件

        progressSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * 拖动条停止拖动的时候调用
             */
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                int progress = arg0.getProgress();
                if (progress == RemoteListContant.PROGRESS_MAX_VALUE) {
                    stopRemoteListPlayer();
                    handlePlaySegmentOver();
                    return;
                }
                if (currentClickItemFile != null) {
                    long beginTime = currentClickItemFile.getBeginTime();
                    long endTime = currentClickItemFile.getEndTime();
                    long avg = (endTime - beginTime) / RemoteListContant.PROGRESS_MAX_VALUE;
                    long trackTime = beginTime + (progress * avg);
                    seekInit(true, false);
                    progressBar.setProgress(progress);

                    LogUtil.i(TAG, "onSeekBarStopTracking, begin time:"
                            + beginTime + " endtime:" + endTime
                            + " avg:" + avg + " MAX:"
                            + RemoteListContant.PROGRESS_MAX_VALUE
                            + " tracktime:" + trackTime);
                    if (mPlayer != null) {
                        Calendar seekTime = Calendar.getInstance();
                        seekTime.setTime(new Date(trackTime));

                        mPlayer.seekPlayback(seekTime);
                    }
                }
            }

            /**
             * 拖动条开始拖动的时候调用
             */
            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            /**
             * 拖动条进度改变的时候调用
             */
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                if (currentClickItemFile != null) {
                    long time = currentClickItemFile.getEndTime() - currentClickItemFile.getBeginTime();
                    int diffSeconds = (int) (time * arg1 / 1000) / 1000;
                    String convToUIDuration = RemoteListUtil.convToUIDuration(diffSeconds);
                    beginTimeTV.setText(convToUIDuration);
                }
            }
        });

        downShake.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                downLayout.clearAnimation();
            }
        });

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtil.d(TAG, "onReceive:" + intent.getAction());
            }
        };
        IntentFilter filter = new IntentFilter();
        getContext().registerReceiver(mReceiver, filter);
    }

    protected void rightEditEvent() {
        // TODO Auto-generated method stub

    }

    // 退出编辑状态
    private void exitEditStatus() {
        selDateImage.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);
        deleteVideoText.setVisibility(View.GONE);
        sectionAdapter.clearAllSelectedCloudFiles();
        sectionAdapter.setEdit(false);
        arrayAdapter.notifyDataSetChanged();
        pinnedHeaderListView.startAnimation();
    }

    /**
     * <p>
     * 退出该页面
     * </p>
     *
     * @author hanlieng 2014-8-4 上午9:04:24
     */
    private void onExitCurrentPage() {
        notPause = true;
        stopQueryTask();
        closePlayBack();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            mScreenOrientationHelper.portrait();
            return;
        }
        if (backBtn != null && backBtn.getVisibility() == View.GONE) {
            exitEditStatus();
        } else {
            onExitCurrentPage();
        }

        if (mReceiver != null) {
            getContext().unregisterReceiver(mReceiver);
        }
        closePlayBack();
        if (mPlayer != null) {
            getOpenSDK().releasePlayer(mPlayer);
        }
        stopQueryTask();
        removeHandler(handler);
        removeHandler(playBackHandler);
    }

    public void removeHandler(Handler handler) {
        this.handler = handler;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    private boolean getWifiType() {
        if (ConnectionDetector.getConnectionType(getContext()) == ConnectionDetector.WIFI) {
            return false;
        } else {
            return true;
        }
    }

    private void stopRemoteListPlayer() {
        try {
            if(mPlayer != null) {
                mPlayer.stopPlayback();
                mPlayer.stopLocalRecord();
            }
            mRealFlow = localInfo.getLimitFlow();
            mStreamFlow = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAnimation(final ImageButton imageButton, float startX, float startY, final float endX,
                                final float endY) {
        AnimationSet set = new AnimationSet(false);
        TranslateAnimation translateAnimationX = new TranslateAnimation(startX, endX, 0, 0);
        translateAnimationX.setInterpolator(new LinearInterpolator());
        TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0, startY, endY);
        translateAnimationY.setInterpolator(new AccelerateInterpolator());
        set.addAnimation(translateAnimationY);
        set.addAnimation(translateAnimationX);
        set.setDuration(1000);
        imageButton.startAnimation(set);
        set.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Message msg = handler.obtainMessage();
                msg.what = ANIMATION_UPDATE;
                msg.obj = imageButton;
                handler.sendMessage(msg);
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ANIMATION_UPDATE:
                    ImageButton imageButton = (ImageButton) msg.obj;
                    if (downShake == null || downLayout == null || imageButton == null || downloadingNumber == null) {
                        return;
                    }
                    downLayout.startAnimation(downShake);
                    imageButton.setVisibility(View.GONE);
                    ViewGroup parent = (ViewGroup) imageButton.getParent();
                    parent.removeView(imageButton);
                    if (downloadingNumber.getVisibility() == View.INVISIBLE) {
                        downloadingNumber.setVisibility(View.VISIBLE);
                    }
                    startGifAnimation();
                    break;
                default:
                    break;
            }
        }

    };

    // 切换到日历界面
    private void goToCalendar() {
        if (getMinDate() != null && new Date().before(getMinDate())) {
            showToast(getContext(),R.string.calendar_setting_error);
            return;
        }
        showDatePicker();
    }

    private void showDatePicker() {
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(queryDate);
        DatePickerDialog dpd = new DatePickerDialog(getContext(), null, nowCalendar.get(Calendar.YEAR),
                nowCalendar.get(Calendar.MONTH), nowCalendar.get(Calendar.DAY_OF_MONTH));

        dpd.setCancelable(true);
        dpd.setTitle(R.string.select_date);
        dpd.setCanceledOnTouchOutside(true);
        dpd.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.certain),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dg, int which) {
                        DatePicker dp = null;
                        Field[] fields = dg.getClass().getDeclaredFields();
                        for (Field field : fields) {
                            field.setAccessible(true);
                            if (field.getName().equals("mDatePicker")) {
                                try {
                                    dp = (DatePicker) field.get(dg);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        dp.clearFocus();
                        Calendar selectCalendar = Calendar.getInstance();
                        selectCalendar.set(Calendar.YEAR, dp.getYear());
                        selectCalendar.set(Calendar.MONTH, dp.getMonth());
                        selectCalendar.set(Calendar.DAY_OF_MONTH, dp.getDayOfMonth());
                        rightEditView.setVisibility(View.GONE);
                        isDateSelected = true;
                        mIsLocalDataQueryPerformed = false;
                        mCheckBtnCloud.setChecked(true);
                        queryDate = (Date) selectCalendar.getTime();
                        onDateChanged();
                    }
                });
        dpd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogUtil.d("Picker", "Cancel!");
                        dialog.dismiss();
                    }
                });

        dpd.show();
    }
    private Date getMinDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse("2012-01-01");
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void onDateChanged() {
        onQueryExceptionLayoutTouched();
    }

    private void onQueryExceptionLayoutTouched() {
        mTitleBar.setTitle(RemoteListUtil.converToMonthAndDay(queryDate));
        pinnedHeaderListView.setVisibility(View.GONE);
        queryExceptionLayout.setVisibility(View.GONE);
        stopQueryTask();
        if (arrayAdapter!=null){
            arrayAdapter.clearData();
            arrayAdapter.clear();
            arrayAdapter.notifyDataSetChanged();
        }
        arrayAdapter = null;
        sectionAdapter = null;
        hasShowListViewLine(false);
        //查询SD卡录像
        if(!mIsLocalDataQueryPerformed)
        {
            // 当云视频文件不超过100000个不会出现异常，超过即异常
            mIsLocalDataQueryPerformed = true;
            int cloudTotal = 100000;
            hasShowListViewLine(false);
            mWaitDlg.show();
            stopQueryTask();
            queryPlayBackLocalListAsyncTask = new QueryPlayBackLocalListAsyncTask(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), this,getContext());
            queryPlayBackLocalListAsyncTask.setQueryDate(queryDate);
            queryPlayBackLocalListAsyncTask.setOnlyHasLocal(true);
            queryPlayBackLocalListAsyncTask.execute(String.valueOf(cloudTotal));
        }
        mContentTabDeviceRl.setVisibility(true? View.VISIBLE : View.GONE);
        mCheckBtnCloud.setChecked(false);
    }

    private void hasShowListViewLine(boolean isShow) {
        if (isShow) {
            mDatabind.getRoot().findViewById(R.id.listview_line).setVisibility(View.VISIBLE);
        } else {
            mDatabind.getRoot().findViewById(R.id.listview_line).setVisibility(View.INVISIBLE);
        }
    }

    private void stopQueryTask() {
        if (queryPlayBackCloudListAsyncTask != null) {
            queryPlayBackCloudListAsyncTask.cancel(true);
            queryPlayBackCloudListAsyncTask.setAbort(true);
            queryPlayBackCloudListAsyncTask = null;
        }

        if (queryPlayBackLocalListAsyncTask != null) {
            queryPlayBackLocalListAsyncTask.cancel(true);
            queryPlayBackLocalListAsyncTask.setAbort(true);
            queryPlayBackLocalListAsyncTask = null;
        }
    }

    private void initUi() {
        mContentTabCloudRl = (RelativeLayout)mDatabind.getRoot().findViewById(R.id.content_tab_cloud_root);
        mContentTabDeviceRl = (RelativeLayout) mDatabind.getRoot().findViewById(R.id.content_tab_device_root);
        mCheckBtnCloud = (CheckTextButton) mDatabind.getRoot().findViewById(R.id.pb_search_tab_btn_cloud);
        mCheckBtnDevice = (CheckTextButton) mDatabind.getRoot().findViewById(R.id.pb_search_tab_btn_device);
        mTabContentMainFrame = (FrameLayout) mDatabind.getRoot().findViewById(R.id.ez_tab_content_frame);
        mRecordRotateViewUtil = new RotateViewUtil();
        mCheckBtnDevice.setToggleEnable(false);
        mCheckBtnCloud.setToggleEnable(false);
        mCheckBtnCloud.setChecked(true);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.getId() == R.id.pb_search_tab_btn_cloud) {
                    mContentTabCloudRl.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    mCheckBtnDevice.setChecked(!isChecked);
                } else if((buttonView.getId() == R.id.pb_search_tab_btn_device) ) {
                    mContentTabDeviceRl.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    mCheckBtnCloud.setChecked(!isChecked);
                }
            }
        };
        mCheckBtnDevice.setOnCheckedChangeListener(onCheckedChangeListener);
        mCheckBtnCloud.setOnCheckedChangeListener(onCheckedChangeListener);

        mCheckBtnCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(!mCheckBtnCloud.isChecked()) {
                    mCheckBtnCloud.setChecked(true);
                }
            }
        });
        mCheckBtnDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(!mCheckBtnDevice.isChecked()) {
                    mCheckBtnDevice.setChecked(true);

                    // If not query local data ever, do it once
                    if(!mIsLocalDataQueryPerformed)
                    {
                        // 当云视频文件不超过100000个不会出现异常，超过即异常
                        mIsLocalDataQueryPerformed = true;
                        int cloudTotal = 100000;
                        hasShowListViewLine(false);
                        mWaitDlg.show();
                        stopQueryTask();
                        queryPlayBackLocalListAsyncTask = new QueryPlayBackLocalListAsyncTask(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), PlaybackFragment.this,getContext());
                        queryPlayBackLocalListAsyncTask.setQueryDate(queryDate);
                        queryPlayBackLocalListAsyncTask.setOnlyHasLocal(true);
                        queryPlayBackLocalListAsyncTask.execute(String.valueOf(cloudTotal));
                    }
                }
            }
        });

        pinnedHeaderListView = (PinnedHeaderListView) mDatabind.getRoot().findViewById(R.id.listView);
        mPinnedHeaderListViewForLocal = (PinnedHeaderListView) mDatabind.getRoot().findViewById(R.id.listView_device);
        remoteListPage = (RelativeLayout) mDatabind.getRoot().findViewById(R.id.remote_list_page);
        mTitleBar = (TitleBar) mDatabind.getRoot().findViewById(R.id.title);
        /** 测量状态栏高度 **/
        ViewTreeObserver viewTreeObserver = remoteListPage.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mRemotePlayBackRect == null) {
                    // 获取状况栏高度
                    mRemotePlayBackRect = new Rect();
                    getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(mRemotePlayBackRect);
                }
            }
        });
        queryExceptionLayout = (LinearLayout) mDatabind.getRoot().findViewById(R.id.query_exception_ly);
        novideoImg = (LinearLayout) mDatabind.getRoot().findViewById(R.id.novideo_img);
        mNoVideoImgLocal = (LinearLayout) mDatabind.getRoot().findViewById(R.id.novideo_img_device);
        loadingBar = (LoadingTextView) mDatabind.getRoot().findViewById(R.id.loadingTextView);
        loadingBar.setText(R.string.loading_text_default);
        remoteLoadingBufferTv = (TextView) mDatabind.getRoot().findViewById(R.id.remote_loading_buffer_tv);
        touchLoadingBufferTv = (TextView) mDatabind.getRoot().findViewById(R.id.touch_loading_buffer_tv);
        remotePlayBackArea = (RelativeLayout) mDatabind.getRoot().findViewById(R.id.remote_playback_area);
        endTimeTV = (TextView) mDatabind.getRoot().findViewById(R.id.end_time_tv);
        exitBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.exit_btn);
        surfaceView = (SurfaceView) mDatabind.getRoot().findViewById(R.id.remote_playback_wnd_sv);
        surfaceView.getHolder().addCallback(this);
        mRemotePlayBackRatioTv = (TextView) mDatabind.getRoot().findViewById(R.id.remoteplayback_ratio_tv);
        mRemotePlayBackTouchListener = new CustomTouchListener() {

            @Override
            public boolean canZoom(float scale) {
                return false;
            }

            @Override
            public boolean canDrag(int direction) {
                if (mPlayScale != 1) {
                    return true;
                }
                return false;
            }

            @Override
            public void onSingleClick() {
                onPlayAreaTouched();
            }

            @Override
            public void onDoubleClick(View view, MotionEvent motionEvent) {

            }


            @Override
            public void onZoom(float scale) {
            }

            @Override
            public void onDrag(int direction, float distance, float rate) {
                LogUtil.d(TAG, "onDrag:" + direction);
            }

            @Override
            public void onEnd(int mode) {
                LogUtil.d(TAG, "onEnd:" + mode);
            }

            @Override
            public void onZoomChange(float scale, CustomRect oRect, CustomRect curRect) {
                LogUtil.d(TAG, "onZoomChange:" + scale);
                if (status == RemoteListContant.STATUS_PLAYING) {
                    if (scale > 1.0f && scale < 1.1f) {
                        scale = 1.1f;
                    }
                    setPlayScaleUI(scale, oRect, curRect);
                }
            }
        };
        surfaceView.setOnTouchListener(mRemotePlayBackTouchListener);
        setRemoteListSvLayout();
        mRemotePlayBackRecordLy = (LinearLayout) mDatabind.getRoot().findViewById(R.id.remoteplayback_record_ly);
        progressSeekbar = (SeekBar) mDatabind.getRoot().findViewById(R.id.progress_seekbar);
        progressBar = (ProgressBar) mDatabind.getRoot().findViewById(R.id.progressbar);
        beginTimeTV = (TextView) mDatabind.getRoot().findViewById(R.id.begin_time_tv);
        controlArea = (LinearLayout) mDatabind.getRoot().findViewById(R.id.control_area);
        progressArea = (LinearLayout) mDatabind.getRoot().findViewById(R.id.progress_area);
        captureBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.remote_playback_capture_btn);
        videoRecordingBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.remote_playback_video_recording_btn);
        videoRecordingBtn_end = mDatabind.getRoot().findViewById(R.id.remote_playback_video_recording_btn_end);
        mRealPlayRecordContainer = mDatabind.getRoot().findViewById(R.id.playback_video_frame);
        downloadBtn = (LinearLayout) mDatabind.getRoot().findViewById(R.id.remote_playback_download_btn);
        downLayout = (RelativeLayout) mDatabind.getRoot().findViewById(R.id.down_layout);
        fileSizeText = (TextView) mDatabind.getRoot().findViewById(R.id.file_size_text);
        deleteVideoText = (TextView) mDatabind.getRoot().findViewById(R.id.delete_playback);
        measure(downloadBtn);
        measure(downLayout);
        measure(controlArea);
        downloading = (ImageView) mDatabind.getRoot().findViewById(R.id.downloading);
        downDrawable = ((AnimationDrawable) downloading.getBackground());
        downloadingNumber = (TextView) mDatabind.getRoot().findViewById(R.id.downloading_number);
        loadingImgView = (LoadingView) mDatabind.getRoot().findViewById(R.id.remote_loading_iv);
        loadingPbLayout = (LinearLayout) mDatabind.getRoot().findViewById(R.id.loading_pb_ly);
        flowTV = (TextView) mDatabind.getRoot().findViewById(R.id.remote_playback_flow_tv);

        errorInfoTV = (TextView) mDatabind.getRoot().findViewById(R.id.error_info_tv);
        errorReplay = (ImageButton) mDatabind.getRoot().findViewById(R.id.error_replay_btn);
        loadingPlayBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.loading_play_btn);
        pauseBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.remote_playback_pause_btn);
        soundBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.remote_playback_sound_btn);
        replayAndNextArea = (LinearLayout) mDatabind.getRoot().findViewById(R.id.re_next_area);
        mRemotePlayBackRecordIv = (ImageView) mDatabind.getRoot().findViewById(R.id.remoteplayback_record_iv);
        mRemotePlayBackRecordTv = (TextView) mDatabind.getRoot().findViewById(R.id.remoteplayback_record_tv);
        replayBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.replay_btn);
        nextPlayBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.next_play_btn);
        progressSeekbar.setMax(RemoteListContant.PROGRESS_MAX_VALUE);
        progressBar.setMax(RemoteListContant.PROGRESS_MAX_VALUE);
        matteImage = (ImageView) mDatabind.getRoot().findViewById(R.id.matte_image);

        autoLayout = (LinearLayout) mDatabind.getRoot().findViewById(R.id.auto_play_layout);
        autoLayout.setVisibility(View.GONE);
        textTime = (TextView) mDatabind.getRoot().findViewById(R.id.time_text);
        cancelBtn = (Button) mDatabind.getRoot().findViewById(R.id.cancel_auto_play_btn);
        cancelBtn.setOnClickListener(this);
        touchProgressLayout = (LinearLayout) mDatabind.getRoot().findViewById(R.id.touch_progress_layout);
        showDownLoad();
        mFullscreenButton = (CheckTextButton) mDatabind.getRoot().findViewById(R.id.fullscreen_button);
        mScreenOrientationHelper = new ScreenOrientationHelper(getActivity(), mFullscreenButton);
        notPause = true;
        mControlBarRL = (RelativeLayout) mDatabind.getRoot().findViewById(R.id.flow_area);

        mLandscapeTitleBar = (TitleBar) mDatabind.getRoot().findViewById(R.id.pb_title_bar_landscape);
        mLandscapeTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff), getResources().getDrawable(R.color.dark_bg_70p),
                getResources().getDrawable(R.drawable.message_back_selector_1));
        mLandscapeTitleBar.setOnTouchListener(this);
        if(mCameraInfo != null) {
            mLandscapeTitleBar.setTitle(mCameraInfo.getCameraName());
        }
        mLandscapeTitleBar.addBackButton(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                onBackPressed();
            }
        });
    }

    private void startGifAnimation() {
        if (!downDrawable.isRunning()) {
            downDrawable = (AnimationDrawable) downloading.getBackground();
            downDrawable.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mScreenOrientationHelper.postOnStop();
        LogUtil.d(TAG, "onStop():" + notPause + " status:" + status);

        if (status == RemoteListContant.STATUS_PLAY || status == RemoteListContant.STATUS_PLAYING
                || status == RemoteListContant.STATUS_PAUSE) {
        }
        if (notPause) {
            closePlayBack();
        }
    }

    private void closePlayBack() {
        if (status == RemoteListContant.STATUS_EXIT_PAGE) {
            return;
        }
        LogUtil.d(TAG, "停止运行.........");
        stopRemoteListPlayer();
        onActivityStopUI();
        stopUpdateTimer();
        status = RemoteListContant.STATUS_EXIT_PAGE;
        if(surfaceView != null) {
            surfaceView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume()");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(surfaceView.getWindowToken(), 0);
            }
        }, 200);
        int downCount = 0;//downloadHelper.getDownloadCountInQueue();
        downloadingNumber.setText("" + downCount);
        if (downCount <= 0) {
            downLayout.setVisibility(View.INVISIBLE);
            downloadingNumber.setVisibility(View.INVISIBLE);
        } else {
            startGifAnimation();
        }
        // 判断是否处理暂停状态
        if (notPause || status == RemoteListContant.STATUS_DECRYPT) {
            surfaceView.setVisibility(View.VISIBLE);
            onActivityResume();
            startUpdateTimer();
            isDateSelected = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mScreenOrientationHelper.postOnStart();
    }

    /**
     * 启动定时器
     *
     * @see
     * @since V1.0
     */
    private void startUpdateTimer() {
        stopUpdateTimer();
        // 开始录像计时
        mUpdateTimer = new Timer();
        mUpdateTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (controlArea.getVisibility() == View.VISIBLE && mControlDisplaySec < 5
                        && status != RemoteListContant.STATUS_INIT) {
                    mControlDisplaySec++;
                }
                // 流量提醒
                if (mLimitFlowDialog != null && mLimitFlowDialog.isShowing() && mCountDown > 0) {
                    mCountDown--;
                }
                // 录像显示
                if (bIsRecording) {
                    // 更新录像时间
                    Calendar OSDTime = null;
                    if(mPlayer != null) {
                        OSDTime = mPlayer.getOSDTime();
                    }
                    if (OSDTime != null) {
                        String playtime = Utils.OSD2Time(OSDTime);
                        if (!playtime.equals(mRecordTime)) {
                            mRecordSecond++;
                            mRecordTime = playtime;
                        }
                    }
                }
                sendMessage(RemoteListContant.MSG_REMOTELIST_UI_UPDATE, 0, 0);
            }
        };
        // 延时1000ms后执行，1000ms执行一次
        mUpdateTimer.schedule(mUpdateTimerTask, 0, 1000);
    }

    private void sendMessage(int message, int arg1, int arg2) {
        if (playBackHandler != null) {
            Message msg = playBackHandler.obtainMessage();
            msg.what = message;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            playBackHandler.sendMessage(msg);
        }
    }

    // 页面可见
    private void onActivityResume() {
        if (!isDateSelected && currentClickItemFile != null) {
            if (currentClickItemFile.getUiPlayTimeOnStop() != null) {
                int type = currentClickItemFile.getType();
                Calendar uiPlayTimeOnStop = currentClickItemFile.getUiPlayTimeOnStop();
                reConnectPlay(type, uiPlayTimeOnStop);
            } else if (status == RemoteListContant.STATUS_EXIT_PAGE || status == RemoteListContant.STATUS_DECRYPT) {
                onReplayBtnClick();
            }
        }
    }

    // 停止定时器
    private void stopUpdateTimer() {
        mControlDisplaySec = 0;
        // 停止录像计时
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }

        if (mUpdateTimerTask != null) {
            mUpdateTimerTask.cancel();
            mUpdateTimerTask = null;
        }
    }

    // 页面不可见时UI
    private void onActivityStopUI() {
        if(exitBtn != null) {
            exitBtn.setVisibility(View.GONE);
        }
        if(controlArea != null) {
            controlArea.setVisibility(View.GONE);
        }
        if(progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        mControlDisplaySec = 0;
        notShowControlArea = true;
    }

    private void getData() {
        localInfo = LocalInfo.getInstance();
        Bundle bundle = getArguments();
        if (bundle != null) {
            queryDate = DateTimeUtil.getNow();
            mCameraInfo = bundle.getParcelable(IntentConsts.EXTRA_CAMERA_INFO);
        }
        mAudioPlayUtil = AudioPlayUtil.getInstance(App.Companion.getInstance());
        DisplayMetrics metric = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
        localInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        localInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));
        mRealFlow = localInfo.getLimitFlow();
        sharedPreferences = getContext().getSharedPreferences(Constant.VIDEOGO_PREFERENCE_NAME, 0);
        isCloudPrompt = sharedPreferences.getBoolean(HAS_BEAN_CLOUD_PROMPT, true);
        downShake = AnimationUtils.loadAnimation(getContext(), R.anim.button_shake);
        downShake.reset();
        downShake.setFillAfter(true);
    }

    @Override
    public void queryHasNoData() {
        // todo ,remove all other novideoImg.setVisibility
        showTab(R.id.novideo_img);
    }

    @Override
    public void queryOnlyHasLocalFile() {
        hasShowListViewLine(false);
        stopQueryTask();
        queryPlayBackLocalListAsyncTask = new QueryPlayBackLocalListAsyncTask(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), this,getContext());
        queryPlayBackLocalListAsyncTask.setQueryDate(queryDate);
        queryPlayBackLocalListAsyncTask.setOnlyHasLocal(true);
        queryPlayBackLocalListAsyncTask.execute(String.valueOf(0));
    }

    // 录像查询为空UI显示
    private void queryNoDataUIDisplay() {
        loadingBar.setVisibility(View.GONE);
        novideoImg.setVisibility(View.VISIBLE);
        showTab(R.id.novideo_img);
    }

    @Override
    public void queryLocalException() {
    }

    @Override
    public void queryCloudSucess(List<CloudPartInfoFileEx> cloudPartInfoFileEx, int queryMLocalStatus,
                                 List<CloudPartInfoFile> cloudPartInfoFile) {
        rightEditView.setVisibility(View.VISIBLE);
        mDatabind.getRoot().findViewById(R.id.display_layout).setVisibility(View.VISIBLE);
        hasShowListViewLine(true);
        loadingBar.setVisibility(View.GONE);
        pinnedHeaderListView.setVisibility(View.VISIBLE);
        showTab(R.id.ez_tab_content_frame);
        if (queryMLocalStatus == RemoteListContant.HAS_LOCAL) {
            CloudPartInfoFileEx partInfoFileEx = new CloudPartInfoFileEx();
            partInfoFileEx.setMore(true);
            cloudPartInfoFileEx.add(partInfoFileEx);
        }
        arrayAdapter = new StandardArrayAdapter(getContext(), R.id.layout, cloudPartInfoFileEx);
        arrayAdapter.setAdapterChangeListener(this);
        sectionAdapter = new SectionListAdapter(getContext(),getLayoutInflater(), arrayAdapter,mCameraInfo.getDeviceSerial());
        pinnedHeaderListView.setAdapter(sectionAdapter);

        pinnedHeaderListView.setOnScrollListener(sectionAdapter);
        pinnedHeaderListView.setPinnedHeaderView(getLayoutInflater().inflate(R.layout.list_section,
                pinnedHeaderListView, false));
        pinnedHeaderListView.startAnimation();
        sectionAdapter.setOnHikItemClickListener(this);
    }

    @Override
    public void queryLocalSucess(List<CloudPartInfoFileEx> cloudPartInfoFileEx, int position,
                                 List<CloudPartInfoFile> cloudPartInfoFile) {
        hasShowListViewLine(true);
        showTab(R.id.content_tab_device_root);
        mPinnedHeaderListViewForLocal.setVisibility(View.VISIBLE);
        if (mArrayAdapterForLocal != null) {
            mArrayAdapterForLocal.addLoacalFileExAll(cloudPartInfoFileEx);
            mArrayAdapterForLocal.notifyDataSetChanged();
            int selPosition = mArrayAdapterForLocal.getCloudFileEx().size() - 2;
            if (getAndroidOSVersion() < 14) {
                mPinnedHeaderListViewForLocal.setSelection(selPosition > 0 ? selPosition : 0);
            } else {
                mPinnedHeaderListViewForLocal.smoothScrollToPositionFromTop(selPosition > 0 ? selPosition : 0, 100, 500);
            }
        } else {
            mArrayAdapterForLocal = new StandardArrayAdapter(getContext(), R.id.layout, cloudPartInfoFileEx);
            mArrayAdapterForLocal.setAdapterChangeListener(this);
            mSectionAdapterForLocal = new SectionListAdapter(getContext(),getLayoutInflater(), mArrayAdapterForLocal, mCameraInfo.getDeviceSerial());
            mPinnedHeaderListViewForLocal.setAdapter(mSectionAdapterForLocal);
            mPinnedHeaderListViewForLocal.setOnScrollListener(mSectionAdapterForLocal);
            mSectionAdapterForLocal.setOnHikItemClickListener(this);
        }
    }


    @Override
    public void queryOnlyLocalNoData() {
        queryNoDataUIDisplay();
        showTab(R.id.novideo_img_device);
    }

    @Override
    public void queryLocalNoData() {
        showTab(R.id.novideo_img_device);
    }

    @Override
    public void queryException() {
        loadingBar.setVisibility(View.GONE);
        queryExceptionLayout.setVisibility(View.VISIBLE);
        mDatabind.getRoot().findViewById(R.id.display_layout).setVisibility(View.GONE);
    }

    @Override
    public void queryTaskOver(int type, int queryMode, int queryErrorCode, String detail) {
        if (type == RemoteListContant.TYPE_CLOUD) {
            LogUtil.e(TAG, "queryTaskOver: TYPE_CLOUD");
        } else if (type == RemoteListContant.TYPE_LOCAL) {
            if (mWaitDlg != null && mWaitDlg.isShowing()) {
                mWaitDlg.dismiss();
            }
            LogUtil.e(TAG, "queryTaskOver: TYPE_LOCAL");
            queryPlayBackLocalListAsyncTask = null;
        }
    }

    private int getAndroidOSVersion() {
        int osVersion;
        try {
            osVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            osVersion = 0;
        }
        return osVersion;
    }

    private void convertCloudPartInfoFile2EZCloudRecordFile(EZCloudRecordFile dst, CloudPartInfoFile src) {
        dst.setCoverPic(src.getPicUrl());
        dst.setDownloadPath(src.getDownloadPath());
        dst.setFileId(src.getFileId());
        dst.setEncryption(src.getKeyCheckSum());
        dst.setStartTime(Utils.convert14Calender(src.getStartTime()));
        dst.setStopTime(Utils.convert14Calender(src.getEndTime()));;
    }
    private void convertCloudPartInfoFile2EZDeviceRecordFile(EZDeviceRecordFile dst, CloudPartInfoFile src) {
        dst.setStartTime(Utils.convert14Calender(src.getStartTime()));
        dst.setStopTime(Utils.convert14Calender(src.getEndTime()));
    }

    @Override
    public void onHikItemClickListener(CloudPartInfoFile cloudFile, ClickedListItem playClickItem) {
        if (autoLayout.getVisibility() == View.VISIBLE) {
            autoLayout.setVisibility(View.GONE);
        }
        fileSizeText.setText("");
        newPlayInit(true, true);
        showControlArea(true);
        timeBucketUIInit(playClickItem.getBeginTime(), playClickItem.getEndTime());
        currentClickItemFile = playClickItem;
        mDeviceRecordInfo = null;
        mCloudRecordInfo = null;
        if (!cloudFile.isCloud()) {
            RemoteFileInfo fileInfo = cloudFile.getRemoteFileInfo();
            this.fileInfo = fileInfo.copy();
            mDeviceRecordInfo = new EZDeviceRecordFile();
            convertCloudPartInfoFile2EZDeviceRecordFile(mDeviceRecordInfo, cloudFile);
            mSectionAdapterForLocal.setSelection(cloudFile.getPosition());
            if (getAndroidOSVersion() < 14) {
                mPinnedHeaderListViewForLocal.setSelection(playClickItem.getPosition());
            } else {
                mPinnedHeaderListViewForLocal.smoothScrollToPositionFromTop(playClickItem.getPosition(), 100, 500);
            }
            mPlayer.setHandler(playBackHandler);
            mPlayer.setSurfaceHold(surfaceView.getHolder());
            mPlayer.startPlayback(mDeviceRecordInfo);
        } else {
            sectionAdapter.setSelection(cloudFile.getPosition());
            if (getAndroidOSVersion() < 14) {
                pinnedHeaderListView.setSelection(playClickItem.getPosition());
            } else {
                pinnedHeaderListView.smoothScrollToPositionFromTop(playClickItem.getPosition(), 100, 500);
            }
            if (!isCloudPrompt && downPopup != null) {
                isCloudPrompt = true;
                sharedPreferences.edit().putBoolean(HAS_BEAN_CLOUD_PROMPT, true).commit();
                matteImage.setVisibility(View.VISIBLE);
                downPopup.showAsDropDown(downloadBtn, 0 + Utils.dip2px(getContext(), 7), -downloadBtn.getMeasuredHeight()
                        + Utils.dip2px(getContext(), 7));
                mScreenOrientationHelper.disableSensorOrientation();
            } else {
                mCloudRecordInfo = new EZCloudRecordFile();
                convertCloudPartInfoFile2EZCloudRecordFile(mCloudRecordInfo, cloudFile);
                mPlayer.setHandler(playBackHandler);
                mPlayer.setSurfaceHold(surfaceView.getHolder());
                mPlayer.startPlayback(mCloudRecordInfo);
            }
        }
        showDownLoad();
    }

    private void measure(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
    }

    private void newSeekPlayUIInit() {
        touchProgressLayout.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        exitBtn.setVisibility(View.GONE);
        replayAndNextArea.setVisibility(View.GONE);
        errorInfoTV.setVisibility(View.GONE);
        errorReplay.setVisibility(View.GONE);
        // 加载百分比重置
        remoteLoadingBufferTv.setText("0%");
        touchLoadingBufferTv.setText("0%");
        notShowControlArea = false;
        controlArea.setVisibility(View.VISIBLE);
        progressArea.setVisibility(View.GONE);
        mControlDisplaySec = 0;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            captureBtn.setVisibility(View.GONE);
            videoRecordingBtn.setVisibility(View.VISIBLE);
        } else {
            captureBtn.setVisibility(View.VISIBLE);
            videoRecordingBtn.setVisibility(View.VISIBLE);
            captureBtn.setEnabled(false);
            videoRecordingBtn.setEnabled(false);
        }
        loadingPlayBtn.setVisibility(View.GONE);
    }

    // 新的播放UI初始化
    private void newPlayUIInit() {
        remotePlayBackArea.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.INVISIBLE);
        surfaceView.setVisibility(View.VISIBLE);
        loadingImgView.setVisibility(View.VISIBLE);
        loadingPbLayout.setVisibility(View.VISIBLE);
        touchProgressLayout.setVisibility(View.GONE);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        exitBtn.setVisibility(View.GONE);
        replayAndNextArea.setVisibility(View.GONE);
        errorInfoTV.setVisibility(View.GONE);
        errorReplay.setVisibility(View.GONE);
        // 加载百分比重置
        remoteLoadingBufferTv.setText("0%");
        touchLoadingBufferTv.setText("0%");
        notShowControlArea = false;
        controlArea.setVisibility(View.VISIBLE);
        progressArea.setVisibility(View.GONE);
        mControlDisplaySec = 0;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            captureBtn.setVisibility(View.GONE);
            videoRecordingBtn.setVisibility(View.VISIBLE);
            mControlBarRL.setVisibility(View.VISIBLE);
        } else {
            captureBtn.setVisibility(View.VISIBLE);
            videoRecordingBtn.setVisibility(View.VISIBLE);
            captureBtn.setEnabled(false);
            videoRecordingBtn.setEnabled(false);
            mControlBarRL.setVisibility(View.GONE);
        }
        loadingPlayBtn.setVisibility(View.GONE);
    }

    private void newPlayInit(boolean resetPause, boolean resetProgress) {
        if (mShowNetworkTip) {
            mShowNetworkTip = false;
        }
        initEZPlayer();
        newPlayUIInit();
        if (resetPause) {
            resetPauseBtnUI();
        }
        if (resetProgress) {
            progressBar.setProgress(0);
            progressSeekbar.setProgress(0);
        }
        if (localInfo.isSoundOpen()) {
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundon_btn_selector);
        } else {
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundoff_btn_selector);
        }
    }

    private void seekInit(boolean resetPause, boolean resetProgress) {
        newSeekPlayUIInit();
        if (resetPause) {
            resetPauseBtnUI();
        }
        if (resetProgress) {
            progressBar.setProgress(0);
            progressSeekbar.setProgress(0);
        }
        if (localInfo.isSoundOpen()) {
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundon_btn_selector);
        } else {
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundoff_btn_selector);
        }
    }

    // 重置暂停按钮 UI和状态值
    private void resetPauseBtnUI() {
        notPause = true;
        pauseBtn.setBackgroundResource(R.drawable.ez_remote_list_pause_btn_selector);
    }

    @Override
    public void onHikMoreClickListener(boolean isExpand) {
        if (isExpand) {
            if (arrayAdapter != null && arrayAdapter.getLocalFileEx() != null) {
                arrayAdapter.addLoacalFileExAll();
                arrayAdapter.notifyDataSetChanged();
                int position = arrayAdapter.getCloudFileEx().size() - 1;
                if (getAndroidOSVersion() < 14) {
                    pinnedHeaderListView.setSelection(position > 0 ? position : 0);
                } else {
                    pinnedHeaderListView.smoothScrollToPositionFromTop(position > 0 ? position : 0, 100, 500);
                }
            } else {
                // 当云视频文件不超过100000个不会出现异常，超过即异常
                int cloudTotal = 100000;
                hasShowListViewLine(false);
                mWaitDlg.show();
                stopQueryTask();
                queryPlayBackLocalListAsyncTask = new QueryPlayBackLocalListAsyncTask(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), this,getContext());
                queryPlayBackLocalListAsyncTask.setQueryDate(queryDate);
                queryPlayBackLocalListAsyncTask.setOnlyHasLocal(true);
                queryPlayBackLocalListAsyncTask.execute(String.valueOf(cloudTotal));
            }
        } else {
            if (arrayAdapter != null) {
                arrayAdapter.minusLocalFileExAll();
            }
        }

    }

    // 暂停按钮实现停止
    private void pauseStop() {
        status = RemoteListContant.STATUS_STOP;
        stopRemoteListPlayer();
        loadingImgView.setVisibility(View.GONE);
        loadingPbLayout.setVisibility(View.GONE);

        loadingPlayBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(mPlayer != null) {
            mPlayer.setSurfaceHold(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mPlayer != null) {
            mPlayer.setSurfaceHold(null);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = newConfig.orientation;
        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }

    private void onOrientationChanged() {
        showDownLoad();
        setRemoteListSvLayout();
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // 显示状态栏
            fullScreen(false);
            if (status != RemoteListContant.STATUS_PLAYING) {
                // 不允许选择屏幕
                mScreenOrientationHelper.disableSensorOrientation();
            }
            // 竖屏处理
            remoteListPage.setBackgroundColor(getResources().getColor(R.color.white));
            mTitleBar.setVisibility(View.VISIBLE);
            pinnedHeaderListView.setVisibility(View.VISIBLE);
            if (controlArea.getVisibility() == View.VISIBLE) {
                exitBtn.setVisibility(View.VISIBLE);
                captureBtn.setVisibility(View.GONE);
                videoRecordingBtn.setVisibility(View.VISIBLE);
            }
            mControlBarRL.setVisibility(View.VISIBLE);
            mLandscapeTitleBar.setVisibility(View.GONE);
        } else {
            // 横屏处理
            // 隐藏状态栏
            fullScreen(true);
            remoteListPage.setBackgroundColor(getResources().getColor(R.color.black_bg));
            mTitleBar.setVisibility(View.GONE);
            pinnedHeaderListView.setVisibility(View.GONE);
            exitBtn.setVisibility(View.GONE);
            captureBtn.setVisibility(View.VISIBLE);
            videoRecordingBtn.setVisibility(View.VISIBLE);
            mControlBarRL.setVisibility(View.GONE);
            mLandscapeTitleBar.setVisibility(View.VISIBLE);
        }
    }

    private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getActivity().getWindow().setAttributes(lp);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getActivity().getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActivity().getWindow().setAttributes(attr);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    // 是否显示下载图标
    private void showDownLoad() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT /*&& downloadHelper.getDownloadCountInQueue() > */) {
            downLayout.setVisibility(View.VISIBLE);
        } else {
            downLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // TODO
            case R.id.query_exception_ly:
                onQueryExceptionLayoutTouched();
                break;
            case R.id.cancel_auto_play_btn:
                autoLayout.setVisibility(View.GONE);
                break;
            case R.id.loading_play_btn:
                notPause = true;
                pauseBtn.setBackgroundResource(R.drawable.remote_list_pause_btn_selector);
                pausePlay();
                break;
            case R.id.error_replay_btn:
            case R.id.replay_btn:
                onReplayBtnClick();
                break;
            case R.id.next_play_btn:
                break;
            case R.id.remote_playback_pause_btn:
                onPlayPauseBtnClick();
                break;
            case R.id.remote_playback_sound_btn:
                onSoundBtnClick();
                break;
            case R.id.remote_playback_capture_btn:
                onCapturePicBtnClick();
                break;
            case R.id.remote_playback_video_recording_btn:
            case R.id.remote_playback_video_recording_btn_end:
                onRecordBtnClick();
                break;
            case R.id.exit_btn:
                onPlayExitBtnOnClick();
                break;
            case R.id.control_area:
                break;
            case R.id.delete_playback:
                if (sectionAdapter != null && sectionAdapter.getSelectedCloudFiles().size() < 1) {
                }
                break;
            default:
                break;
        }
    }

    // 暂停按钮事件处理
    private void onPlayPauseBtnClick() {
        if (notPause) {
            // 暂停播放
            notPause = false;
            pauseBtn.setBackgroundResource(R.drawable.remote_list_play_btn_selector);
            if (status != RemoteListContant.STATUS_PLAYING) {
                pauseStop();
            } else {
                status = RemoteListContant.STATUS_PAUSE;
                if(mPlayer != null) {
                    // 停止录像
                    stopRemotePlayBackRecord();
                    mPlayer.pausePlayback();
                }
            }
        } else {
            notPause = true;
            pauseBtn.setBackgroundResource(R.drawable.ez_remote_list_pause_btn_selector);
            if (status != RemoteListContant.STATUS_PAUSE) {
                pausePlay();
            } else {
                if(mPlayer != null) {
                    mPlayer.resumePlayback();
                }
                mScreenOrientationHelper.disableSensorOrientation();
                status = RemoteListContant.STATUS_PLAYING;
            }
        }
    }

    // 重播当前录像片段
    private void onReplayBtnClick() {
        newPlayInit(true, true);
        timeBucketUIInit(currentClickItemFile.getBeginTime(), currentClickItemFile.getEndTime());
        int type = currentClickItemFile.getType();
        if (type == RemoteListContant.TYPE_CLOUD) {
        }
    }

    // 开始录像
    private void onRecordBtnClick() {
        mControlDisplaySec = 0;
        if (bIsRecording) {
            stopRemotePlayBackRecord();
            mRemotePlayBackRecordLy.setVisibility(View.GONE);
            bIsRecording = !bIsRecording;
            mRemotePlayBackRecordTv.setText("00:00");
            mRecordSecond = 0;
            return;
        }

        bIsRecording = !bIsRecording;
        if (!SDCardUtil.isSDCardUseable()) {
            mRemotePlayBackRecordLy.setVisibility(View.GONE);
            mRemotePlayBackRecordTv.setText("00:00");
            mRecordSecond = 0;
            // 提示SD卡不可用
            showToast(getContext(),R.string.remoteplayback_SDCard_disable_use);
            return;
        }

        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
            mRemotePlayBackRecordLy.setVisibility(View.GONE);
            mRemotePlayBackRecordTv.setText("00:00");
            mRecordSecond = 0;
            // 提示内存不足
            showToast(getContext(),R.string.remoteplayback_record_fail_for_memory);
            return;
        }
        mRemotePlayBackRecordLy.setVisibility(View.VISIBLE);
        mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);

        if(mPlayer != null) {
            // 可以采用deviceSerial+时间作为文件命名，demo中简化，只用时间命名
            Date date = new Date();
            String strRecordFile = DEFAULT_SAVE_VIDEO_PATH + mCameraInfo.getCameraName()+"/"
                    + String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) + ".mp4";
            mPlayer.startLocalRecordWithFile(strRecordFile);
            //保存路径
            List<VideoFilePath> files = DBManager.getInstance().get(VideoFilePath.class);
            if (files.size()>=200){
                DBManager.getInstance().delete(VideoFilePath.class,"path=",new String[]{files.get(0).getPath()});
            }
            String name = String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) +".jpg";
            DBManager.getInstance().insert(new VideoFilePath(strRecordFile, name));
            mRecordRotateViewUtil.applyRotation(mRealPlayRecordContainer, videoRecordingBtn, videoRecordingBtn_end, 0, 90);
        }
    }

    // 抓拍按钮响应函数
    private void onCapturePicBtnClick() {
        java.util.Date date = new java.util.Date();
        String path = DEFAULT_SAVE_IMAGE_PATH +mCameraInfo.getCameraName()+"/"
                + String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) +".jpg";
        mControlDisplaySec = 0;
        if (!SDCardUtil.isSDCardUseable()) {
            // 提示SD卡不可用
            showToast(getContext(),R.string.remoteplayback_SDCard_disable_use);
            return;
        }
        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
            // 提示内存不足
            showToast(getContext(),R.string.remoteplayback_capture_fail_for_memory);
            return;
        }
        mCaptureDisplaySec = 4;
        Thread thr = new Thread() {
            @Override
            public void run() {
                if (mPlayer == null) {
                    return;
                }
                String serial = !TextUtils.isEmpty(mCameraInfo.getDeviceSerial()) ? mCameraInfo.getDeviceSerial() : "123456789";
                Bitmap bmp = mPlayer.capturePicture();
                if(bmp != null) {
                    try {
                        mAudioPlayUtil.playAudioFile(AudioPlayUtil.CAPTURE_SOUND);
                        if (TextUtils.isEmpty(path)) {
                            bmp.recycle();
                            bmp = null;
                            return;
                        }
                        EZUtils.saveCapturePictrue(path, bmp);
                        //保存路径
                        List<PicFilePath> files = DBManager.getInstance().get(PicFilePath.class);
                        if (files.size()>=200){
                            DBManager.getInstance().delete(PicFilePath.class, "path=?", new String[]{files.get(0).getPath()});
                        }
                        String name = String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) +".jpg";
                        DBManager.getInstance().insert(new PicFilePath(path, name));
                        MediaScanner mMediaScanner = new MediaScanner(getContext());
                        mMediaScanner.scanFile(path, "jpg");
                        getActivity().runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), getResources().getString(R.string.already_saved_to_volume), Toast.LENGTH_SHORT).show();
                            }});
                    } catch (InnerException e) {
                        e.printStackTrace();
                    } finally {
                        if(bmp != null){
                            bmp.recycle();
                            bmp = null;
                            return;
                        }
                    }
                }
                super.run();
            }};
        thr.start();
    }

    // 声音按钮
    private void onSoundBtnClick() {
        if(mPlayer == null) {
            return;
        }

        if (localInfo.isSoundOpen()) {
            // 关闭声音
            localInfo.setSoundOpen(false);
            mPlayer.closeSound();
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundoff_btn_selector);
        } else {
            // 打开声音
            localInfo.setSoundOpen(true);
            mPlayer.openSound();
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundon_btn_selector);
        }
    }

    private void pausePlay() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // 不允许选择屏幕
            mScreenOrientationHelper.disableSensorOrientation();
        }
        Calendar seekTime = getTimeBarSeekTime();
        Calendar osdTime = null;
        if(mPlayer != null) {
            osdTime = mPlayer.getOSDTime();
        }
        Calendar startTime = Calendar.getInstance();
        long playTime = 0L;
        if (osdTime != null) {
            playTime = osdTime.getTimeInMillis();
        } else {
            playTime = seekTime.getTimeInMillis();
        }
        startTime.setTimeInMillis(playTime);
        LogUtil.i(TAG, "pausePlay:" + startTime);
        if (currentClickItemFile != null) {
            reConnectPlay(currentClickItemFile.getType(), startTime);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.remote_playback_area:
                onPlayAreaTouched();
                break;
            case R.id.control_area:
                break;
            case R.id.query_exception_ly:
                onQueryExceptionLayoutTouched();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onSelectedChangeListener(int total) {
        if (total > 0) {
            deleteVideoText.setText(getString(R.string.delete) + "(" + total + ")");
        } else {
            deleteVideoText.setText(R.string.delete);
        }
    }

    @Override
    public void onDeleteCloudFileCompleteListener(boolean isLocal) {
        // TODO Auto-generated method stub
        rightEditView.setVisibility(View.GONE);
        if (isLocal) {
            onHikMoreClickListener(true);
            sectionAdapter.setExpand(true);
        } else {
            pinnedHeaderListView.setVisibility(View.GONE);
            hasShowListViewLine(false);
            queryNoDataUIDisplay();
        }

    }


    @Override
    public void onInputVerifyCode(final String verifyCode) {
        if (mPlayer != null) {
            String name = mCameraInfo.getDeviceSerial()+ String.valueOf(mCameraInfo.getCameraNo());
            mVerifyCode = verifyCode;
            if (mVerifyCode == null){
                DBManager.getInstance().insert(new Verifcode(name, verifyCode));
            }else{
                DBManager.getInstance().update(new Verifcode(name, verifyCode),"name=?", new String[]{name});
            }

            newPlayUIInit();
            showControlArea(true);

            if (mDeviceRecordInfo != null) {
                if (mPlayer != null){
                    LogUtil.d(TAG, "verify code is " + verifyCode);
                    mPlayer.setPlayVerifyCode(mVerifyCode);
                }
                mPlayer.startPlayback(mDeviceRecordInfo.getStartTime(), mDeviceRecordInfo.getStopTime());
            } else if (mCloudRecordInfo != null) {
                if (mPlayer != null){
                    mPlayer.setPlayVerifyCode(mVerifyCode);
                }
                mPlayer.startPlayback(mCloudRecordInfo);
            }
        }
    }


}
