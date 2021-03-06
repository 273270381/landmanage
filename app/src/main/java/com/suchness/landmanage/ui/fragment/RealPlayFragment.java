package com.suchness.landmanage.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.suchness.deeplearningapp.app.base.BaseFragment;
import com.suchness.landmanage.R;
import com.suchness.landmanage.app.App;
import com.suchness.landmanage.app.utils.ActivityUtils;
import com.suchness.landmanage.app.utils.AudioPlayUtil;
import com.suchness.landmanage.app.utils.EZUtils;
import com.suchness.landmanage.app.utils.RealPlaySquareInfo;
import com.suchness.landmanage.app.utils.VerifyCodeInput;
import com.suchness.landmanage.app.weight.loadtextview.LoadingTextView;
import com.suchness.landmanage.app.weight.realplay.ScreenOrientationHelper;
import com.suchness.landmanage.data.been.db.DBManager;
import com.suchness.landmanage.data.been.db.PicFilePath;
import com.suchness.landmanage.data.been.db.Verifcode;
import com.suchness.landmanage.data.been.db.VideoFilePath;
import com.suchness.landmanage.databinding.FragmentRealPlayBinding;
import com.suchness.landmanage.viewmodel.HomeViewModel;
import com.videogo.constant.Config;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.InnerException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.realplay.RealPlayStatus;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.MediaScanner;
import com.videogo.util.RotateViewUtil;
import com.videogo.util.SDCardUtil;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.CustomRect;
import com.videogo.widget.CustomTouchListener;
import com.videogo.widget.RingView;
import com.videogo.widget.TitleBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import static com.blankj.utilcode.util.Utils.runOnUiThread;
import static com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_IMAGE_PATH;
import static com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_VIDEO_PATH;
import static com.suchness.landmanage.app.ext.LoadingDialogExtKt.getOpenSDK;

/**
 * @author: hejunfeng
 * @date: 2021/12/20 0020
 */
public class RealPlayFragment extends BaseFragment<HomeViewModel, FragmentRealPlayBinding> implements View.OnClickListener, SurfaceHolder.Callback,
        Handler.Callback, View.OnTouchListener, VerifyCodeInput.VerifyCodeInputListener{

    @BindView(R.id.realplay_page_ly)
    LinearLayout mRealPlayPageLy;
    @BindView(R.id.title_bar_portrait)
    TitleBar mPortraitTitleBar;
    @BindView(R.id.title_bar_landscape)
    TitleBar mLandscapeTitleBar;
    @BindView(R.id.realplay_play_rl)
    RelativeLayout mRealPlayPlayRl;
    @BindView(R.id.realplay_sv)
    SurfaceView mRealPlaySv;
    @BindView(R.id.realplay_prompt_rl)
    RelativeLayout mRealPlayPromptRl;
    @BindView(R.id.realplay_loading_rl)
    RelativeLayout mRealPlayLoadingRl;
    @BindView(R.id.realplay_tip_tv)
    TextView mRealPlayTipTv;
    @BindView(R.id.realplay_play_iv)
    ImageView mRealPlayPlayIv;
    @BindView(R.id.realplay_loading)
    LoadingTextView mRealPlayPlayLoading;
    @BindView(R.id.realplay_privacy_ly)
    LinearLayout mRealPlayPlayPrivacyLy;
    @BindView(R.id.realplay_page_anim_iv)
    ImageView mPageAnimIv;
    @BindView(R.id.realplay_ptz_direction_iv)
    ImageView mRealPlayPtzDirectionIv;
    @BindView(R.id.realplay_control_rl)
    LinearLayout mRealPlayControlRl;
    @BindView(R.id.realplay_play_btn)
    ImageButton mRealPlayBtn;
    @BindView(R.id.realplay_sound_btn)
    ImageButton mRealPlaySoundBtn;
    @BindView(R.id.realplay_flow_tv)
    TextView mRealPlayFlowTv;
    @BindView(R.id.realplay_capture_rl)
    RelativeLayout mRealPlayCaptureRl;
    @BindView(R.id.realplay_capture_iv)
    ImageView mRealPlayCaptureIv;
    @BindView(R.id.realplay_capture_watermark_iv)
    ImageView mRealPlayCaptureWatermarkIv;
    @BindView(R.id.realplay_record_ly)
    LinearLayout mRealPlayRecordLy;
    @BindView(R.id.realplay_record_iv)
    ImageView mRealPlayRecordIv;
    @BindView(R.id.realplay_record_tv)
    TextView mRealPlayRecordTv;
    @BindView(R.id.realplay_quality_btn)
    Button mRealPlayQualityBtn;
    @BindView(R.id.realplay_full_flow_ly)
    LinearLayout mRealPlayFullFlowLy;
    @BindView(R.id.realplay_full_rate_tv)
    TextView mRealPlayFullRateTv;
    @BindView(R.id.realplay_full_flow_tv)
    TextView mRealPlayFullFlowTv;
    @BindView(R.id.realplay_ratio_tv)
    TextView mRealPlayRatioTv;
    @BindView(R.id.fullscreen_button)
    CheckTextButton mFullscreenButton;
    @BindView(R.id.fullscreen_full_button)
    CheckTextButton mFullscreenFullButton;
    @BindView(R.id.realplay_full_operate_bar)
    RelativeLayout mRealPlayFullOperateBar;
    @BindView(R.id.read_ltz_wnd_landscape)
    RelativeLayout mRead_ptz_wnd_landscape;
    @BindView(R.id.realplay_full_play_btn)
    ImageButton mRealPlayFullPlayBtn;
    @BindView(R.id.realplay_full_sound_btn)
    ImageButton mRealPlayFullSoundBtn;
    @BindView(R.id.realplay_full_talk_btn)
    ImageButton mRealPlayFullTalkBtn;
    @BindView(R.id.realplay_full_previously_btn)
    ImageButton mRealPlayFullCaptureBtn;
    @BindView(R.id.realplay_full_ptz_btn)
    ImageButton mRealPlayFullPtzBtn;
    @BindView(R.id.realplay_full_video_container)
    View mRealPlayFullRecordContainer;
    @BindView(R.id.realplay_full_video_container_land)
    View mRealPlayFullRecordContainer_land;
    @BindView(R.id.realplay_full_video_btn)
    ImageButton mRealPlayFullRecordBtn;
    @BindView(R.id.realplay_full_video_btn_land)
    ImageButton mRealPlayFullRecordBtn_land;
    @BindView(R.id.realplay_full_video_start_btn)
    ImageButton mRealPlayFullRecordStartBtn;
    @BindView(R.id.realplay_full_video_start_btn_land)
    ImageButton mRealPlayFullRecordStartBtn_land;
    @BindView(R.id.realplay_full_ptz_anim_btn)
    ImageButton mRealPlayFullPtzAnimBtn;
    @BindView(R.id.realplay_full_ptz_prompt_iv)
    ImageView mRealPlayFullPtzPromptIv;
    @BindView(R.id.realplay_full_talk_anim_btn)
    ImageButton mRealPlayFullTalkAnimBtn;
    @BindView(R.id.realplay_full_anim_btn)
    ImageButton mRealPlayFullAnimBtn;



    private static final String TAG = "RealPlayerActivity";
    /**
     * ????????????
     */
    private static final int ANIMATION_DURING_TIME = 500;
    // UI??????
    public static final int MSG_PLAY_UI_UPDATE = 200;
    public static final int MSG_AUTO_START_PLAY = 202;
    public static final int MSG_CLOSE_PTZ_PROMPT = 203;
    public static final int MSG_HIDE_PTZ_DIRECTION = 204;
    public static final int MSG_HIDE_PAGE_ANIM = 205;
    public static final int MSG_PLAY_UI_REFRESH = 206;
    public static final int MSG_PREVIEW_START_PLAY = 207;
    public static final int MSG_SET_VEDIOMODE_SUCCESS = 105;
    /**
     * ????????????????????????
     */
    public static final int MSG_SET_VEDIOMODE_FAIL = 106;
    // ????????????URL
    private String mRtspUrl = null;
    // ????????????????????????
    private RealPlaySquareInfo mRealPlaySquareInfo = null;
    private AudioPlayUtil mAudioPlayUtil = null;
    private LocalInfo mLocalInfo = null;
    private Handler mHandler = null;
    private float mRealRatio = Constant.LIVE_VIEW_RATIO;
    /**
     * ????????????????????????
     */
    private int mStatus = RealPlayStatus.STATUS_INIT;
    private boolean mIsOnStop = false;
    /**
     * ??????????????????
     */
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    private int mForceOrientation = 0;
    private Rect mRealPlayRect = null;
    private String titlename="";
    private Button mTiletRightBtn = null;
    private SurfaceHolder mRealPlaySh = null;
    private CustomTouchListener mRealPlayTouchListener = null;
    private int mControlDisplaySec = 0;
    // ????????????
    private float mPlayScale = 1;
    private RelativeLayout.LayoutParams mRealPlayCaptureRlLp = null;
    private int mCaptureDisplaySec = 0;
    private boolean mIsRecording = false;
    private String mRecordTime = null;
    private AnimationDrawable mPageAnimDrawable = null;
    /**
     * ????????????
     */
    private int mRecordSecond = 0;
    private HorizontalScrollView mRealPlayOperateBar = null;
    private LinearLayout mRealPlayPtzBtnLy = null;
    private LinearLayout mRealPlayTalkBtnLy = null;
    private LinearLayout mRealPlaySslBtnLy = null;
    private LinearLayout mRealPlayCaptureBtnLy = null;
    private LinearLayout mRealPlayRecordContainerLy = null;
    private ImageButton mRealPlayPtzBtn = null;
    private ImageButton mRealPlayTalkBtn = null;
    private Button mRealPlaySslBtn = null;
    private ImageButton mRealPlayPrivacyBtn = null;
    private ImageButton mRealPlayCaptureBtn = null;
    private View mRealPlayRecordContainer = null;
    private ImageButton mRealPlayRecordBtn = null;
    private ImageButton mRealPlayRecordStartBtn = null;
    private RotateViewUtil mRecordRotateViewUtil = null;
    private RotateViewUtil mRecordRotateViewUtil_land = null;

    // ????????????
    private boolean mIsOnPtz = false;
    private int[] mStartXy = new int[2];
    private int[] mEndXy = new int[2];
    private PopupWindow mQualityPopupWindow = null;
    private PopupWindow mPtzPopupWindow = null;
    private LinearLayout mPtzControlLy = null;
    private LinearLayout mPtzControlLy_land = null;
    private PopupWindow mTalkPopupWindow = null;
    private RingView mTalkRingView = null;
    private Button mTalkBackControlBtn = null;
    /**
     * ???????????????????????????
     */
    private RealPlayBroadcastReceiver mBroadcastReceiver = null;
    /**
     * ?????????
     */
    private Timer mUpdateTimer = null;
    /**
     * ????????????????????????
     */
    private TimerTask mUpdateTimerTask = null;
    private ScreenOrientationHelper mScreenOrientationHelper;
    // ??????????????????
    private float mZoomScale = 0;
    // ????????????
    private boolean mIsOnTalk = false;
    // ????????????
    private TextView mRealPlayPreviewTv = null;
    /**
     * ???????????????????????????
     */
    private EZPlayer mEZPlayer = null;
    private CheckTextButton mFullScreenTitleBarBackBtn;
    private EZConstants.EZVideoLevel mCurrentQulityMode = EZConstants.EZVideoLevel.VIDEO_LEVEL_HD;
    private EZDeviceInfo mDeviceInfo = null;
    private EZCameraInfo mCameraInfo = null;
    private String mVerifyCode ;
    /**
     * ????????????????????????
     */
    private long mStreamFlow = 0;
    //????????????
    private ImageView tx_speed;
    private boolean isFaster = false;
    private int my_speed = EZConstants.PTZ_SPEED_DEFAULT;

    @Override
    public int layoutId() {
        return R.layout.fragment_real_play;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return;
        }
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mRealPlaySv != null) {
                    InputMethodManager imm = (InputMethodManager)App.Companion.getInstance().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mRealPlaySv.getWindowToken(), 0);
                }
            }
        }, 200);
        initUI();
        mRealPlaySv.setVisibility(View.VISIBLE);
        LogUtil.i(TAG, "onResume real play status:" + mStatus);
        if (mCameraInfo != null && mDeviceInfo != null && mDeviceInfo.getStatus() != 1) {
            if (mStatus != RealPlayStatus.STATUS_STOP) {
                stopRealPlay();
            }
            setRealPlayFailUI(getString(R.string.realplay_fail_device_not_exist));
        } else {
            if (mStatus == RealPlayStatus.STATUS_INIT || mStatus == RealPlayStatus.STATUS_PAUSE
                    || mStatus == RealPlayStatus.STATUS_DECRYPT) {
                // ????????????
                startRealPlay();
            }
        }
        mIsOnStop = false;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mScreenOrientationHelper != null) {
            mScreenOrientationHelper.postOnStop();
        }
        mHandler.removeMessages(MSG_AUTO_START_PLAY);
        hidePageAnim();

        if (mCameraInfo == null && mRtspUrl == null) {
            return;
        }

        closePtzPopupWindow();
        closeTalkPopupWindow(true, false);
        if (mStatus != RealPlayStatus.STATUS_STOP) {
            mIsOnStop = true;
            stopRealPlay();
            mStatus = RealPlayStatus.STATUS_PAUSE;
            setRealPlayStopUI();
        } else {
            setStopLoading();
        }
        mRealPlaySv.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEZPlayer != null) {
            mEZPlayer.release();
        }
        mHandler.removeMessages(MSG_AUTO_START_PLAY);
        mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
        mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
        mHandler.removeMessages(MSG_HIDE_PAGE_ANIM);
        mHandler = null;
        if (mBroadcastReceiver != null) {
            // ???????????????????????????
            getContext().unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        mScreenOrientationHelper = null;
    }
    private void exit() {
        closePtzPopupWindow();
        closeTalkPopupWindow(true, false);
        if (mStatus != RealPlayStatus.STATUS_STOP) {
            stopRealPlay();
            setRealPlayStopUI();
        }
        mHandler.removeMessages(MSG_AUTO_START_PLAY);
        mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
        mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
        mHandler.removeMessages(MSG_HIDE_PAGE_ANIM);
        if (mBroadcastReceiver != null) {
            // ???????????????????????????
            getContext().unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
//        finish();
    }

//
//    public void finish() {
//        if (mCameraInfo != null) {
//            Intent intent = new Intent();
//            intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, mCameraInfo.getDeviceSerial());
//            intent.putExtra(IntentConsts.EXTRA_CAMERA_NO, mCameraInfo.getCameraNo());
//            intent.putExtra("video_level", mCameraInfo.getVideoLevel().getVideoLevel());
//            setResult(EZCameraListActivity.RESULT_CODE, intent);
//        }
//        super.finish();
//    }




//    @Override
//    public void onBackPressed() {
//        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
//            mScreenOrientationHelper.portrait();
//            return;
//        }
//        exit();
//    }


    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void initWidget() {
        // ??????????????????

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initTitleBar();
        initRealPlayPageLy();
        initLoadingUI();
        mRealPlaySh = mRealPlaySv.getHolder();
        mRealPlaySh.addCallback(this);
        mRealPlayTouchListener = new CustomTouchListener() {
            @Override
            public boolean canZoom(float scale) {
                if (mStatus == RealPlayStatus.STATUS_PLAY) {
                    return true;
                } else {
                    return false;
                }
            }
            @Override
            public boolean canDrag(int direction) {
                if (mStatus != RealPlayStatus.STATUS_PLAY) {
                    return false;
                }
                if (mEZPlayer != null && mDeviceInfo != null) {
                    // ????????????
                    if (DRAG_LEFT == direction || DRAG_RIGHT == direction) {
                        // ??????/??????????????????
                        if (mDeviceInfo.isSupportPTZ()) {
                            return true;
                        }
                    } else if (DRAG_UP == direction || DRAG_DOWN == direction) {
                        // ??????/??????????????????
                        if (mDeviceInfo.isSupportPTZ()) {
                            return true;
                        }
                    }
                }
                return false;
            }
            @Override
            public void onSingleClick() {
                onRealPlaySvClick();
            }

            @Override
            public void onDoubleClick(View view, MotionEvent motionEvent) {

            }


            @Override
            public void onZoom(float scale) {
                LogUtil.d(TAG, "onZoom:" + scale);
                if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportZoom()) {
                    startZoom(scale);
                }
            }

            @Override
            public void onDrag(int direction, float distance, float rate) {
                LogUtil.d(TAG, "onDrag:" + direction);
                if (mEZPlayer != null) {
                    startDrag(direction, distance, rate);
                }
            }

            @Override
            public void onEnd(int mode) {
                LogUtil.d(TAG, "onEnd:" + mode);
                if (mEZPlayer != null) {
                    stopDrag(false);
                }
                if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportZoom()) {
                    stopZoom();
                }
            }

            @Override
            public void onZoomChange(float scale, CustomRect oRect, CustomRect curRect) {
                LogUtil.d(TAG, "onZoomChange:" + scale);
                if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportZoom()) {
                    //??????????????????
                    return;
                }
                if (mStatus == RealPlayStatus.STATUS_PLAY) {
                    if (scale > 1.0f && scale < 1.1f) {
                        scale = 1.1f;
                    }
                    setPlayScaleUI(scale, oRect, curRect);
                }
            }
        };
        mRealPlaySv.setOnTouchListener(mRealPlayTouchListener);
        mRealPlayFlowTv.setText("0k/s");
        mRealPlayCaptureRlLp = (RelativeLayout.LayoutParams) mRealPlayCaptureRl.getLayoutParams();
        mRealPlayFullRateTv.setText("0k/s");
        mRealPlayFullFlowTv.setText("0MB");
        if (mRtspUrl == null) {
            initOperateBarUI(false);
            initFullOperateBarUI();
            mRealPlayOperateBar.setVisibility(View.VISIBLE);
        } else {
            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            realPlayPlayRlLp.gravity = Gravity.CENTER;
            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
            mRealPlayPlayRl.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        setRealPlaySvLayout();
        initCaptureUI();
        mScreenOrientationHelper = new ScreenOrientationHelper(getActivity(), mFullscreenButton, /*mFullscreenFullButton*/mFullScreenTitleBarBackBtn);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = getOpenSDK().captureCamera(mCameraInfo.getDeviceSerial(),mCameraInfo.getCameraNo());
                    Log.d(TAG,"num="+mCameraInfo.getCameraNo());
                    Log.d(TAG,"url="+url);
                } catch (BaseException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    // ?????????????????????
    @Override
    public void initData() {
        // ??????????????????
        Application application = (Application)getActivity().getApplication();
        mAudioPlayUtil = AudioPlayUtil.getInstance(application);
        // ??????????????????????????????
        mLocalInfo = LocalInfo.getInstance();
        // ??????????????????
        DisplayMetrics metric = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
        mLocalInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        mLocalInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));
        mHandler = new Handler(this);
        mRecordRotateViewUtil = new RotateViewUtil();
        mRecordRotateViewUtil_land = new RotateViewUtil();
        mBroadcastReceiver = new RealPlayBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        getContext().registerReceiver(mBroadcastReceiver, filter);
        mRealPlaySquareInfo = new RealPlaySquareInfo();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCameraInfo = bundle.getParcelable(IntentConsts.EXTRA_CAMERA_INFO);
            mDeviceInfo = bundle.getParcelable(IntentConsts.EXTRA_DEVICE_INFO);
            mRtspUrl = bundle.getParcelable(IntentConsts.EXTRA_RTSP_URL);
            titlename = mCameraInfo.getCameraName();
            if (mCameraInfo != null) {
                mCurrentQulityMode = (mCameraInfo.getVideoLevel());
            }
            getRealPlaySquareInfo();
        }
    }

    private void getRealPlaySquareInfo() {
        if (TextUtils.isEmpty(mRtspUrl)) {
            return;
        }
        Uri uri = Uri.parse(mRtspUrl.replaceFirst("&", "?"));
        try {
            mRealPlaySquareInfo.mSquareId = Integer.parseInt(uri.getQueryParameter("squareid"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            mRealPlaySquareInfo.mChannelNo = Integer.parseInt(Utils.getUrlValue(mRtspUrl, "channelno=", "&"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        mRealPlaySquareInfo.mCameraName = uri.getQueryParameter("cameraname");
        try {
            mRealPlaySquareInfo.mSoundType = Integer.parseInt(uri.getQueryParameter("soundtype"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        mRealPlaySquareInfo.mCoverUrl = uri.getQueryParameter("md5Serial");
        if (!TextUtils.isEmpty(mRealPlaySquareInfo.mCoverUrl)) {
            mRealPlaySquareInfo.mCoverUrl = mLocalInfo.getServAddr() + mRealPlaySquareInfo.mCoverUrl + "_mobile.jpeg";
        }
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {

    }


    /**
     * screen?????????????????????
     */
    private class RealPlayBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                closePtzPopupWindow();
                closeTalkPopupWindow(true, false);
                if (mStatus != RealPlayStatus.STATUS_STOP) {
                    stopRealPlay();
                    mStatus = RealPlayStatus.STATUS_PAUSE;
                    setRealPlayStopUI();
                }
            }
        }
    }

    private void initTitleBar() {
        mPortraitTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff),getResources().getDrawable(R.color.colorPrimary),
                null);
//        mPortraitTitleBar.addBackButton(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                closePtzPopupWindow();
//                closeTalkPopupWindow(true, false);
//                if (mStatus != RealPlayStatus.STATUS_STOP) {
//                    stopRealPlay();
//                    setRealPlayStopUI();
//                }
//                finish();
//            }
//        });
        mLandscapeTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff), getResources().getDrawable(R.color.dark_bg_70p),
                getResources().getDrawable(R.drawable.message_back_selector));
        mLandscapeTitleBar.setOnTouchListener(this);
        mFullScreenTitleBarBackBtn = new CheckTextButton(getContext());
        mFullScreenTitleBarBackBtn.setBackground(getResources().getDrawable(R.drawable.common_title_back_selector));
        mLandscapeTitleBar.addLeftView(mFullScreenTitleBarBackBtn);
    }

    private void initRealPlayPageLy() {
        /** ????????????????????? **/
        ViewTreeObserver viewTreeObserver = mRealPlayPageLy.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mRealPlayRect == null) {
                    // ?????????????????????
                    mRealPlayRect = new Rect();
                    getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(mRealPlayRect);
                }
            }
        });
    }


    public void startDrag(int direction, float distance, float rate) {
    }

    public void stopDrag(boolean control) {
    }

    private void startZoom(float scale) {
        if (mEZPlayer == null) {
            return;
        }
        hideControlRlAndFullOperateBar(false);
        boolean preZoomIn = mZoomScale > 1.01 ? true : false;
        boolean zoomIn = scale > 1.01 ? true : false;
        if (mZoomScale != 0 && preZoomIn != zoomIn) {
            mZoomScale = 0;
        }
        if (scale != 0 && (mZoomScale == 0 || preZoomIn != zoomIn)) {
            mZoomScale = scale;
        }
    }

    private void stopZoom() {
        if (mEZPlayer == null) {
            return;
        }
        if (mZoomScale != 0) {
            mZoomScale = 0;
        }
    }

    private void setPtzDirectionIv(int command) {
        setPtzDirectionIv(command, 0);
    }

    private void setPtzDirectionIv(int command, int errorCode) {
        if (command != -1 && errorCode == 0) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            switch (command) {
                case RealPlayStatus.PTZ_LEFT:
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.left_twinkle);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case RealPlayStatus.PTZ_RIGHT:
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.right_twinkle);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case RealPlayStatus.PTZ_UP:
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.up_twinkle);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case RealPlayStatus.PTZ_DOWN:
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.down_twinkle);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.realplay_sv);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                default:
                    break;
            }
            mRealPlayPtzDirectionIv.setVisibility(View.VISIBLE);
            mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
            Message msg = new Message();
            msg.what = MSG_HIDE_PTZ_DIRECTION;
            msg.arg1 = 1;
            mHandler.sendMessageDelayed(msg, 500);
        } else if (errorCode != 0) {
            RelativeLayout.LayoutParams svParams = (RelativeLayout.LayoutParams) mRealPlaySv.getLayoutParams();
            RelativeLayout.LayoutParams params = null;
            switch (errorCode) {
                case ErrorCode.ERROR_CAS_PTZ_ROTATION_LEFT_LIMIT_FAILED:
                    params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, svParams.height);
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.ptz_left_limit);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case ErrorCode.ERROR_CAS_PTZ_ROTATION_RIGHT_LIMIT_FAILED:
                    params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, svParams.height);
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.ptz_right_limit);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case ErrorCode.ERROR_CAS_PTZ_ROTATION_UP_LIMIT_FAILED:
                    params = new RelativeLayout.LayoutParams(svParams.width, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.ptz_top_limit);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case ErrorCode.ERROR_CAS_PTZ_ROTATION_DOWN_LIMIT_FAILED:
                    params = new RelativeLayout.LayoutParams(svParams.width, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.ptz_bottom_limit);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.realplay_sv);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                default:
                    break;
            }
            mRealPlayPtzDirectionIv.setVisibility(View.VISIBLE);
            mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
            Message msg = new Message();
            msg.what = MSG_HIDE_PTZ_DIRECTION;
            msg.arg1 = 1;
            mHandler.sendMessageDelayed(msg, 500);
        } else {
            mRealPlayPtzDirectionIv.setVisibility(View.GONE);
            mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
        }
    }

    private int getSupportPtz() {
        if (mEZPlayer == null || mDeviceInfo == null) {
            return 0;
        }

        if (mDeviceInfo.isSupportPTZ() || mDeviceInfo.isSupportZoom()) {
            return 1;
        } else {
            return 0;
        }
    }

    // ?????????UI
    @SuppressWarnings("deprecation")
    private void initUI() {
        mPageAnimDrawable = null;
        mRealPlaySoundBtn.setVisibility(View.VISIBLE);
        if (mCameraInfo != null) {
            mPortraitTitleBar.setTitle(titlename);
            mLandscapeTitleBar.setTitle(titlename);
            setCameraInfoTiletRightBtn();
            if (mLocalInfo.isSoundOpen()) {
                mRealPlaySoundBtn.setBackgroundResource(R.drawable.ezopen_vertical_preview_sound_selector);
                mRealPlayFullSoundBtn.setBackgroundResource(R.drawable.play_full_soundon_btn_selector);
            } else {
                mRealPlaySoundBtn.setBackgroundResource(R.drawable.ezopen_vertical_preview_sound_off_selector);
                mRealPlayFullSoundBtn.setBackgroundResource(R.drawable.play_full_soundoff_btn_selector);
            }
            mRealPlayCaptureBtnLy.setVisibility(View.VISIBLE);
            mRealPlayFullCaptureBtn.setVisibility(View.VISIBLE);
            mRealPlayRecordContainerLy.setVisibility(View.VISIBLE);
            mRealPlayFullRecordContainer.setVisibility(View.VISIBLE);
            mRealPlayQualityBtn.setVisibility(View.VISIBLE);
            mRealPlayFullSoundBtn.setVisibility(View.VISIBLE);
            mRealPlayFullPtzAnimBtn.setVisibility(View.GONE);
            mRealPlayFullPtzPromptIv.setVisibility(View.GONE);
            updateUI();
        } else if (mRtspUrl != null) {
            if (!TextUtils.isEmpty(mRealPlaySquareInfo.mCameraName)) {
                mPortraitTitleBar.setTitle(mRealPlaySquareInfo.mCameraName);
                mLandscapeTitleBar.setTitle(mRealPlaySquareInfo.mCameraName);
            }
            mRealPlaySoundBtn.setVisibility(View.GONE);
            mRealPlayQualityBtn.setVisibility(View.GONE);
        }
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            updateOperatorUI();
        }
    }

    private void setCameraInfoTiletRightBtn() {
        if (mTiletRightBtn != null && mDeviceInfo != null) {
            if (mDeviceInfo.getStatus() == 1) {
                mTiletRightBtn.setVisibility(View.VISIBLE);
            } else {
                mTiletRightBtn.setVisibility(View.GONE);
            }
        }
    }

    private void initOperateBarUI(boolean bigScreen) {
        bigScreen = false;
        if (mRealPlayOperateBar != null) {
            mRealPlayOperateBar.setVisibility(View.GONE);
            mRealPlayOperateBar = null;
        }
        if (bigScreen) {
            mRealPlayOperateBar = (HorizontalScrollView) mDatabind.getRoot().findViewById(R.id.ezopen_realplay_operate_bar2);
            mDatabind.getRoot().findViewById(R.id.ezopen_realplay_operate_bar).setVisibility(View.GONE);
            mRealPlayPtzBtnLy = (LinearLayout) mDatabind.getRoot().findViewById(R.id.realplay_ptz_btn_ly2);
            mRealPlayTalkBtnLy = (LinearLayout) mDatabind.getRoot().findViewById(R.id.realplay_talk_btn_ly2);
            mRealPlaySslBtnLy = (LinearLayout) mDatabind.getRoot().findViewById(R.id.realplay_ssl_btn_ly2);
            mRealPlayCaptureBtnLy = (LinearLayout) mDatabind.getRoot().findViewById(R.id.realplay_previously_btn_ly2);
            mRealPlayRecordContainerLy = (LinearLayout) mDatabind.getRoot().findViewById(R.id.realplay_video_container_ly2);
            mRealPlayTalkBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.realplay_talk_btn2);
            mRealPlaySslBtn = (Button) mDatabind.getRoot().findViewById(R.id.realplay_ssl_btn2);
            mRealPlayPrivacyBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.realplay_privacy_btn2);
            mRealPlayCaptureBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.realplay_previously_btn2);
            mRealPlayRecordContainer = mDatabind.getRoot().findViewById(R.id.realplay_video_container2);
            mRealPlayRecordBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.realplay_video_btn2);
            mRealPlayRecordStartBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.realplay_video_start_btn2);
            mRealPlayPtzBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.realplay_ptz_btn2);
        } else {
            mRealPlayOperateBar = (HorizontalScrollView) mDatabind.getRoot().findViewById(R.id.ezopen_realplay_operate_bar);
            mDatabind.getRoot().findViewById(R.id.ezopen_realplay_operate_bar2).setVisibility(View.GONE);
            mRealPlayPtzBtnLy = (LinearLayout) mDatabind.getRoot().findViewById(R.id.realplay_ptz_btn_ly);
            mRealPlayTalkBtnLy = (LinearLayout) mDatabind.getRoot().findViewById(R.id.realplay_talk_btn_ly);
            mRealPlaySslBtnLy = (LinearLayout) mDatabind.getRoot().findViewById(R.id.realplay_ssl_btn_ly);
            mRealPlayCaptureBtnLy = (LinearLayout) mDatabind.getRoot().findViewById(R.id.realplay_previously_btn_ly);
            mRealPlayRecordContainerLy = (LinearLayout) mDatabind.getRoot().findViewById(R.id.realplay_video_container_ly);
            mRealPlayTalkBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.realplay_talk_btn);
            mRealPlaySslBtn = (Button) mDatabind.getRoot().findViewById(R.id.realplay_ssl_btn);
            mRealPlayPrivacyBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.realplay_privacy_btn);
            mRealPlayCaptureBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.realplay_previously_btn);
            mRealPlayRecordContainer = mDatabind.getRoot().findViewById(R.id.realplay_video_container);
            mRealPlayRecordBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.realplay_video_btn);
            mRealPlayRecordStartBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.realplay_video_start_btn);
            mRealPlayPtzBtn = (ImageButton) mDatabind.getRoot().findViewById(R.id.realplay_ptz_btn);
        }
        mRealPlayTalkBtn.setEnabled(false);
        mRealPlayOperateBar.setVisibility(View.VISIBLE);
    }

    private void setBigScreenOperateBtnLayout() {
    }

    private void initFullOperateBarUI() {
        mRealPlayFullOperateBar.setOnTouchListener(this);
        mRead_ptz_wnd_landscape.setOnTouchListener(this);
        //??????
        mPtzControlLy_land = mRead_ptz_wnd_landscape.findViewById(R.id.ptz_control_ly_land);
        ImageButton ptzTopBtn_land = (ImageButton) mRead_ptz_wnd_landscape.findViewById(R.id.ptz_top_btn_land);
        ptzTopBtn_land.setOnTouchListener(mOnTouchListener);
        ImageButton ptzBottomBtn_land = (ImageButton) mRead_ptz_wnd_landscape.findViewById(R.id.ptz_bottom_btn_land);
        ptzBottomBtn_land.setOnTouchListener(mOnTouchListener);
        ImageButton ptzLeftBtn_land = (ImageButton) mRead_ptz_wnd_landscape.findViewById(R.id.ptz_left_btn_land);
        ptzLeftBtn_land.setOnTouchListener(mOnTouchListener);
        ImageButton ptzRightBtn_land = (ImageButton) mRead_ptz_wnd_landscape.findViewById(R.id.ptz_right_btn_land);
        ptzRightBtn_land.setOnTouchListener(mOnTouchListener);
        ImageButton tx_zoomin_land = (ImageButton) mRead_ptz_wnd_landscape.findViewById(R.id.tx_zoomin);
        tx_zoomin_land.setOnTouchListener(mOnTouchListener);
        ImageButton tx_zoomout_land = (ImageButton) mRead_ptz_wnd_landscape.findViewById(R.id.tx_zoomout);
        tx_zoomout_land.setOnTouchListener(mOnTouchListener);
        ImageButton tx_pic_btn = (ImageButton) mRead_ptz_wnd_landscape.findViewById(R.id.tx_pic_btn);
        tx_pic_btn.setOnClickListener(this);

    }

    private void startFullBtnAnim(final View animView, final int[] startXy, final int[] endXy,
                                  final Animation.AnimationListener animationListener) {
        animView.setVisibility(View.VISIBLE);
        TranslateAnimation anim = new TranslateAnimation(startXy[0], endXy[0], startXy[1], endXy[1]);
        anim.setAnimationListener(animationListener);
        anim.setDuration(ANIMATION_DURING_TIME);
        animView.startAnimation(anim);
    }

    private void setVideoLevel() {
        if (mCameraInfo == null || mEZPlayer == null || mDeviceInfo == null) {
            return;
        }

        if (mDeviceInfo.getStatus() == 1) {
            mRealPlayQualityBtn.setEnabled(true);
        } else {
            mRealPlayQualityBtn.setEnabled(false);
        }

        /************** ?????????????????? ???????????????????????????????????????????????????????????????????????? *********************/
        mCameraInfo.setVideoLevel(mCurrentQulityMode.getVideoLevel());

        // ???????????????2-?????????1-?????????0-??????
        if (mCurrentQulityMode.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_flunet);
        } else if (mCurrentQulityMode.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_balanced);
        } else if (mCurrentQulityMode.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_hd);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = newConfig.orientation;
        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }

    private void updateOrientation() {
        if (mIsOnTalk) {
            if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                setForceOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (mStatus == RealPlayStatus.STATUS_PLAY) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
        }
    }

    private void updateOperatorUI() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // ???????????????,??????????????????
            fullScreen(false);
            updateOrientation();
            mPortraitTitleBar.setVisibility(View.VISIBLE);
            mLandscapeTitleBar.setVisibility(View.GONE);
            mRealPlayControlRl.setVisibility(View.VISIBLE);
            mRead_ptz_wnd_landscape.setVisibility(View.GONE);
            closePtzPopupWindow();
            if (mRtspUrl == null) {
                mRealPlayOperateBar.setVisibility(View.VISIBLE);
                mRealPlayFullOperateBar.setVisibility(View.GONE);
                mRead_ptz_wnd_landscape.setVisibility(View.GONE);
                mFullscreenFullButton.setVisibility(View.GONE);
                if (mIsRecording) {
                    mRealPlayRecordBtn.setVisibility(View.GONE);
                    mRealPlayRecordStartBtn.setVisibility(View.VISIBLE);
                } else {
                    mRealPlayRecordBtn.setVisibility(View.VISIBLE);
                    mRealPlayRecordStartBtn.setVisibility(View.GONE);
                }
            }
        } else {
            // ???????????????
            fullScreen(true);
            updateOrientation();
            mPortraitTitleBar.setVisibility(View.GONE);
            // hide the
            mRealPlayControlRl.setVisibility(View.GONE);
            if (!mIsOnTalk && !mIsOnPtz) {
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
            if (mRtspUrl == null) {
                mRealPlayPageLy.setBackgroundColor(getResources().getColor(R.color.black_bg));
                mRealPlayOperateBar.setVisibility(View.GONE);
                mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
                if (!mIsOnTalk && !mIsOnPtz) {
                    mFullscreenFullButton.setVisibility(View.GONE);
                }
                Log.d(TAG,"mIsRecording="+mIsRecording);
                if (mIsRecording) {
                    mRealPlayFullRecordBtn.setVisibility(View.GONE);
                    mRealPlayFullRecordBtn_land.setVisibility(View.GONE);
                    mRealPlayFullRecordStartBtn.setVisibility(View.VISIBLE);
                    mRealPlayFullRecordStartBtn_land.setVisibility(View.VISIBLE);
                } else {
                    mRealPlayFullRecordBtn.setVisibility(View.VISIBLE);
                    mRealPlayFullRecordBtn_land.setVisibility(View.VISIBLE);
                    mRealPlayFullRecordStartBtn.setVisibility(View.GONE);
                    mRealPlayFullRecordStartBtn_land.setVisibility(View.GONE);
                }
            }
        }

        closeQualityPopupWindow();
        if (mStatus == RealPlayStatus.STATUS_START) {
            showControlRlAndFullOperateBar();
        }
    }

    private void updatePtzUI() {
        if (!mIsOnPtz) {
            return;
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setFullPtzStopUI(false);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    openPtzPopupWindow(mRealPlayPlayRl);
                }
            });
        } else {
            closePtzPopupWindow();
            setFullPtzStartUI(false);
        }
    }

    private void updateTalkUI() {
        if (!mIsOnTalk) {
            return;
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mRealPlayFullTalkAnimBtn != null) {
                mRealPlayFullTalkAnimBtn.setVisibility(View.GONE);
                mFullscreenFullButton.setVisibility(View.GONE);
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    openTalkPopupWindow(false);
                }
            });
        } else {
            if (mRealPlayFullTalkAnimBtn != null) {
                mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
                mRealPlayFullOperateBar.post(new Runnable() {
                    @Override
                    public void run() {
                        mRealPlayFullTalkBtn.getLocationInWindow(mStartXy);
                        mEndXy[0] = Utils.dip2px(getContext(), 20);
                        mEndXy[1] = mStartXy[1];
                        mRealPlayFullOperateBar.setVisibility(View.GONE);
                        mRealPlayFullTalkAnimBtn.setVisibility(View.VISIBLE);
                        ((AnimationDrawable) mRealPlayFullTalkAnimBtn.getBackground()).start();
                    }

                });
            }
            closeTalkPopupWindow(false, false);
        }
    }

    private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getActivity().getWindow().setAttributes(lp);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            Log.d("TAG","--??????--");
        } else {
            WindowManager.LayoutParams attr = getActivity().getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getActivity().getWindow().setAttributes(attr);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            Log.d("TAG","--??????--");
        }
    }

    private void onOrientationChanged() {
        mRealPlaySv.setVisibility(View.INVISIBLE);
        setRealPlaySvLayout();
        mRealPlaySv.setVisibility(View.VISIBLE);
        updateOperatorUI();
        updateCaptureUI();
        updateTalkUI();
        updatePtzUI();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(holder);
        }
        mRealPlaySh = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(null);
        }
        mRealPlaySh = null;
    }


    @OnClick({R.id.realplay_play_btn,R.id.realplay_full_play_btn,R.id.realplay_play_iv,R.id.realplay_previously_btn,R.id.realplay_previously_btn2,R.id.realplay_full_previously_btn,
            R.id.tx_pic_btn,R.id.realplay_capture_rl,R.id.realplay_video_btn,R.id.realplay_video_start_btn,R.id.realplay_video_btn2,R.id.realplay_video_start_btn2,R.id.realplay_full_video_btn,
            R.id.realplay_full_video_btn_land,R.id.realplay_full_video_start_btn,R.id.realplay_full_video_start_btn_land,R.id.realplay_talk_btn,R.id.realplay_talk_btn2,R.id.realplay_full_talk_btn,
            R.id.realplay_quality_btn,R.id.realplay_ptz_btn,R.id.realplay_ptz_btn2,R.id.realplay_full_ptz_btn,R.id.ptz_close_btn,R.id.realplay_sound_btn,R.id.realplay_full_sound_btn,R.id.realplay_full_talk_anim_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.realplay_play_btn:
            case R.id.realplay_full_play_btn:
            case R.id.realplay_play_iv:
                if (mStatus != RealPlayStatus.STATUS_STOP) {
                    stopRealPlay();
                    setRealPlayStopUI();
                } else {
                    startRealPlay();
                }
                break;
            case R.id.realplay_previously_btn:
            case R.id.realplay_previously_btn2:
            case R.id.realplay_full_previously_btn:
            case R.id.tx_pic_btn:
                onCapturePicBtnClick();
                break;
            case R.id.realplay_capture_rl:
                break;
            case R.id.realplay_video_btn:
            case R.id.realplay_video_start_btn:
            case R.id.realplay_video_btn2:
            case R.id.realplay_video_start_btn2:
            case R.id.realplay_full_video_btn:
            case R.id.realplay_full_video_btn_land:
            case R.id.realplay_full_video_start_btn:
            case R.id.realplay_full_video_start_btn_land:
                onRecordBtnClick();
                break;
            case R.id.realplay_talk_btn:
            case R.id.realplay_talk_btn2:
            case R.id.realplay_full_talk_btn:
                startVoiceTalk();
                break;
            case R.id.realplay_quality_btn:
                openQualityPopupWindow(mRealPlayQualityBtn);
                break;
            case R.id.realplay_ptz_btn:
            case R.id.realplay_ptz_btn2:
                openPtzPopupWindow(mRealPlayPlayRl);
                break;
            case R.id.realplay_full_ptz_btn:
                setFullPtzStartUI(true);
                break;
            case R.id.ptz_close_btn:
                setFullPtzStopUI(true);
                break;
            case R.id.realplay_sound_btn:
            case R.id.realplay_full_sound_btn:
                onSoundBtnClick();
                break;
            case R.id.realplay_full_talk_anim_btn:
                closeTalkPopupWindow(true, true);
                break;
            default:
                break;
        }
    }

    private void setFullPtzStartUI(boolean startAnim) {
        mIsOnPtz = true;
        setPlayScaleUI(1, null, null);
        if (mLocalInfo.getPtzPromptCount() < 3) {
            mLocalInfo.setPtzPromptCount(mLocalInfo.getPtzPromptCount() + 1);
            mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
            mHandler.sendEmptyMessageDelayed(MSG_CLOSE_PTZ_PROMPT, 2000);
        }
        if (startAnim) {
            onRealPlaySvClick();
            mRealPlayFullOperateBar.setVisibility(View.GONE);
            mRead_ptz_wnd_landscape.setVisibility(View.VISIBLE);
        } else {
            mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
            mRead_ptz_wnd_landscape.setVisibility(View.GONE);
            mRealPlayFullOperateBar.post(new Runnable() {
                @Override
                public void run() {
                    mRealPlayFullPtzBtn.getLocationInWindow(mStartXy);
                    mEndXy[0] = Utils.dip2px(getContext(), 20);
                    mEndXy[1] = mStartXy[1];
                    mRealPlayFullOperateBar.setVisibility(View.GONE);
                    mRealPlayFullPtzAnimBtn.setVisibility(View.VISIBLE);
                }

            });
        }
    }

    private void setFullPtzStopUI(boolean startAnim) {
        mIsOnPtz = false;
        if (startAnim) {
            mRealPlayFullPtzAnimBtn.setVisibility(View.GONE);
            mFullscreenFullButton.setVisibility(View.GONE);
            onRealPlaySvClick();
            mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
            mRead_ptz_wnd_landscape.setVisibility(View.GONE);
        } else {
            mRealPlayFullPtzAnimBtn.setVisibility(View.GONE);
            mFullscreenFullButton.setVisibility(View.GONE);
            mRealPlayFullOperateBar.setVisibility(View.GONE);
            mRead_ptz_wnd_landscape.setVisibility(View.GONE);
        }
        mRealPlayFullPtzPromptIv.setVisibility(View.GONE);
        mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
    }

    private void onSoundBtnClick() {
        if (mLocalInfo.isSoundOpen()) {
            mLocalInfo.setSoundOpen(false);
            mRealPlaySoundBtn.setBackgroundResource(R.drawable.ezopen_vertical_preview_sound_off_selector);
            if (mRealPlayFullSoundBtn != null) {
                mRealPlayFullSoundBtn.setBackgroundResource(R.drawable.play_full_soundoff_btn_selector);
            }
        } else {
            mLocalInfo.setSoundOpen(true);
            mRealPlaySoundBtn.setBackgroundResource(R.drawable.ezopen_vertical_preview_sound_selector);
            if (mRealPlayFullSoundBtn != null) {
                mRealPlayFullSoundBtn.setBackgroundResource(R.drawable.play_full_soundon_btn_selector);
            }
        }
        setRealPlaySound();
    }

    private void setRealPlaySound() {
        if (mEZPlayer != null) {
            if (mRtspUrl == null) {
                if (mLocalInfo.isSoundOpen()) {
                    mEZPlayer.openSound();
                } else {
                    mEZPlayer.closeSound();
                }
            } else {
                if (mRealPlaySquareInfo.mSoundType == 0) {
                    mEZPlayer.closeSound();
                } else {
                    mEZPlayer.openSound();
                }
            }
        }
    }

    /**
     * ????????????
     *
     * @see
     * @since V2.0
     */
    private void startVoiceTalk() {
        LogUtil.d(TAG, "startVoiceTalk");
        if (mEZPlayer == null) {
            LogUtil.d(TAG, "EZPlaer is null");
            return;
        }
        if (mCameraInfo == null) {
            return;
        }
        mIsOnTalk = true;
        updateOrientation();
        Utils.showToast(getContext(), R.string.start_voice_talk);
        mRealPlayTalkBtn.setEnabled(false);
        mRealPlayFullTalkBtn.setEnabled(false);
        mRealPlayFullTalkAnimBtn.setEnabled(false);
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mRealPlayFullAnimBtn.setBackgroundResource(R.drawable.speech_1);
            mRealPlayFullTalkBtn.getLocationInWindow(mStartXy);
            mEndXy[0] = Utils.dip2px(getContext(), 20);
            mEndXy[1] = mStartXy[1];
            startFullBtnAnim(mRealPlayFullAnimBtn, mStartXy, mEndXy, new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Utils.showToast(getContext(), R.string.realplay_full_talk_start_tip);
                    mRealPlayFullTalkAnimBtn.setVisibility(View.VISIBLE);
                    mRealPlayFullAnimBtn.setVisibility(View.GONE);
                    onRealPlaySvClick();
                }
            });
        }
        if (mEZPlayer != null) {
            mEZPlayer.closeSound();
        }
        mEZPlayer.startVoiceTalk();
    }

    /**
     * ????????????
     *
     * @see
     * @since V2.0
     */
    private void stopVoiceTalk(boolean startAnim) {
        if (mCameraInfo == null || mEZPlayer == null) {
            return;
        }
        LogUtil.d(TAG, "stopVoiceTalk");

        mEZPlayer.stopVoiceTalk();
        handleVoiceTalkStoped(startAnim);
    }

    private View.OnClickListener mOnPopWndClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.quality_hd_btn:
                    setQualityMode(EZConstants.EZVideoLevel.VIDEO_LEVEL_HD);
                    break;
                case R.id.quality_balanced_btn:
                    setQualityMode(EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED);
                    break;
                case R.id.quality_flunet_btn:
                    setQualityMode(EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET);
                    break;
                case R.id.ptz_close_btn:
                    closePtzPopupWindow();
                    break;
                case R.id.ptz_flip_btn:
                    break;
                case R.id.talkback_close_btn:
                    closeTalkPopupWindow(true, false);
                    break;
                case R.id.tx_speed:
                    if (isFaster){
                        isFaster = false;
                        my_speed = EZConstants.PTZ_SPEED_DEFAULT;
                        tx_speed.setBackgroundResource(R.mipmap.icon_speed_slow);
                    }else {
                        isFaster = true;
                        my_speed = EZConstants.PTZ_SPEED_FAST;
                        tx_speed.setBackgroundResource(R.mipmap.icon_speed_fast);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * ????????????
     *
     * @param command ptz????????????
     * @param action  ????????????/??????
     */
    private void ptzOption(final EZConstants.EZPTZCommand command, final EZConstants.EZPTZAction action) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ptz_result = false;
                try {
                    ptz_result = getOpenSDK().controlPTZ(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), command,
                            action, my_speed);
                } catch (BaseException e) {
                    e.printStackTrace();
                }
                LogUtil.i(TAG, "controlPTZ ptzCtrl result: " + ptz_result);
            }
        }).start();
    }


    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionevent) {
            boolean ptz_result = false;
            int action = motionevent.getAction();
            final int speed = EZConstants.PTZ_SPEED_DEFAULT;
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    switch (view.getId()) {
                        case R.id.talkback_control_btn:
                            mTalkRingView.setVisibility(View.VISIBLE);
                            mEZPlayer.setVoiceTalkStatus(true);
                            break;
                        case R.id.ptz_top_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_up_sel);
                            setPtzDirectionIv(RealPlayStatus.PTZ_UP);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandUp, EZConstants.EZPTZAction.EZPTZActionSTART);
                            break;
                        case R.id.ptz_bottom_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bottom_sel);
                            setPtzDirectionIv(RealPlayStatus.PTZ_DOWN);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandDown, EZConstants.EZPTZAction.EZPTZActionSTART);
                            break;
                        case R.id.ptz_left_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_left_sel);
                            setPtzDirectionIv(RealPlayStatus.PTZ_LEFT);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandLeft, EZConstants.EZPTZAction.EZPTZActionSTART);
                            break;
                        case R.id.ptz_right_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_right_sel);
                            setPtzDirectionIv(RealPlayStatus.PTZ_RIGHT);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandRight, EZConstants.EZPTZAction.EZPTZActionSTART);
                            break;
                        case R.id.tx_zoomin:
                            setPtzDirectionIv(RealPlayStatus.PTZ_ZOOMIN);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandZoomIn, EZConstants.EZPTZAction.EZPTZActionSTART);
                            Log.i(TAG, "onTouch: zoomin-start");
                            break;
                        case R.id.tx_zoomout:
                            setPtzDirectionIv(RealPlayStatus.PTZ_ZOOMOUT);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandZoomOut, EZConstants.EZPTZAction.EZPTZActionSTART);
                            Log.i(TAG, "onTouch: zoomout-start");
                            break;
                        //??????
                        case R.id.ptz_top_btn_land:
                            Log.i(TAG, "onTouch: top_down");
                            mPtzControlLy_land.setBackgroundResource(R.drawable.ptz_up_land_sel);
                            setPtzDirectionIv(RealPlayStatus.PTZ_UP);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandUp, EZConstants.EZPTZAction.EZPTZActionSTART);
                            break;
                        case R.id.ptz_bottom_btn_land:
                            mPtzControlLy_land.setBackgroundResource(R.drawable.ptz_bottom_land_sel);
                            setPtzDirectionIv(RealPlayStatus.PTZ_DOWN);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandDown, EZConstants.EZPTZAction.EZPTZActionSTART);
                            break;
                        case R.id.ptz_left_btn_land:
                            mPtzControlLy_land.setBackgroundResource(R.drawable.ptz_left_land_sel);
                            setPtzDirectionIv(RealPlayStatus.PTZ_LEFT);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandLeft, EZConstants.EZPTZAction.EZPTZActionSTART);
                            break;
                        case R.id.ptz_right_btn_land:
                            mPtzControlLy_land.setBackgroundResource(R.drawable.ptz_right_land_sel);
                            setPtzDirectionIv(RealPlayStatus.PTZ_RIGHT);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandRight, EZConstants.EZPTZAction.EZPTZActionSTART);
                            break;
                        default:
                            break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    switch (view.getId()) {
                        case R.id.talkback_control_btn:
                            mEZPlayer.setVoiceTalkStatus(false);
                            mTalkRingView.setVisibility(View.GONE);
                            break;
                        case R.id.ptz_top_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandUp, EZConstants.EZPTZAction.EZPTZActionSTOP);
                            break;
                        case R.id.ptz_bottom_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandDown, EZConstants.EZPTZAction.EZPTZActionSTOP);
                            break;
                        case R.id.ptz_left_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandLeft, EZConstants.EZPTZAction.EZPTZActionSTOP);
                            break;
                        case R.id.ptz_right_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandRight, EZConstants.EZPTZAction.EZPTZActionSTOP);
                            break;
                        case R.id.tx_zoomin:
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandZoomIn, EZConstants.EZPTZAction.EZPTZActionSTOP);
                            Log.i(TAG, "onTouch: zoomin-stop");
                            break;
                        case R.id.tx_zoomout:
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandZoomOut, EZConstants.EZPTZAction.EZPTZActionSTOP);
                            Log.i(TAG, "onTouch: zoomout-stop");
                            break;
                        //??????
                        case R.id.ptz_top_btn_land:
                            Log.i(TAG, "onTouch: top_up");
                            mPtzControlLy_land.setBackgroundResource(R.drawable.ptz_bg_1);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandUp, EZConstants.EZPTZAction.EZPTZActionSTOP);
                            break;
                        case R.id.ptz_bottom_btn_land:
                            mPtzControlLy_land.setBackgroundResource(R.drawable.ptz_bg_1);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandDown, EZConstants.EZPTZAction.EZPTZActionSTOP);
                            break;
                        case R.id.ptz_left_btn_land:
                            mPtzControlLy_land.setBackgroundResource(R.drawable.ptz_bg_1);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandLeft, EZConstants.EZPTZAction.EZPTZActionSTOP);
                            break;
                        case R.id.ptz_right_btn_land:
                            mPtzControlLy_land.setBackgroundResource(R.drawable.ptz_bg_1);
                            ptzOption(EZConstants.EZPTZCommand.EZPTZCommandRight, EZConstants.EZPTZAction.EZPTZActionSTOP);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    /**
     * ???????????? ????????? 2-?????????1-?????????0-??????
     *
     * @see
     * @since V2.0
     */
    private void setQualityMode(final EZConstants.EZVideoLevel mode) {
        // ????????????????????????
        if (!ConnectionDetector.isNetworkAvailable(getContext())) {
            // ????????????????????????
            Utils.showToast(getMActivity(), R.string.realplay_set_fail_network);
            return;
        }

        if (mEZPlayer != null) {
            showLoading(this.getString(R.string.setting_video_level));
            Thread thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // need to modify by yudan at 08-11
                        getOpenSDK().setVideoLevel(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), mode.getVideoLevel());
                        mCurrentQulityMode = mode;
                        Message msg = Message.obtain();
                        msg.what = MSG_SET_VEDIOMODE_SUCCESS;
                        mHandler.sendMessage(msg);
                        LogUtil.i(TAG, "setQualityMode success");
                    } catch (BaseException e) {
                        mCurrentQulityMode = EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET;
                        e.printStackTrace();
                        Message msg = Message.obtain();
                        msg.what = MSG_SET_VEDIOMODE_FAIL;
                        mHandler.sendMessage(msg);
                        LogUtil.i(TAG, "setQualityMode fail");
                    }

                }
            }) {
            };
            thr.start();
        }
    }

    /**
     * ????????????????????????
     *
     * @see
     * @since V1.8.3
     */
    private void openTalkPopupWindow(boolean showAnimation) {
        if (mEZPlayer == null && mDeviceInfo == null) {
            return;
        }
        closeTalkPopupWindow(false, false);
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_talkback_wnd, null, true);
        layoutView.setFocusable(true);
        layoutView.setFocusableInTouchMode(true);
        layoutView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (arg1 == KeyEvent.KEYCODE_BACK) {
                    LogUtil.i(TAG, "KEYCODE_BACK DOWN");
                    closeTalkPopupWindow(true, false);
                }
                return false;
            }
        });

        ImageButton talkbackCloseBtn = (ImageButton) layoutView.findViewById(R.id.talkback_close_btn);
        talkbackCloseBtn.setOnClickListener(mOnPopWndClickListener);
        mTalkRingView = (RingView) layoutView.findViewById(R.id.talkback_rv);
        mTalkBackControlBtn = (Button) layoutView.findViewById(R.id.talkback_control_btn);
        mTalkBackControlBtn.setOnTouchListener(mOnTouchListener);

        if (mDeviceInfo.isSupportTalk() == EZConstants.EZTalkbackCapability.EZTalkbackFullDuplex) {
            mTalkRingView.setVisibility(View.VISIBLE);
            mTalkBackControlBtn.setEnabled(false);
            mTalkBackControlBtn.setText(R.string.talking);
        }

        int height = mLocalInfo.getScreenHeight() - mPortraitTitleBar.getHeight() - mRealPlayPlayRl.getHeight()
                - (mRealPlayRect != null ? mRealPlayRect.top : mLocalInfo.getNavigationBarHeight());
        mTalkPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.MATCH_PARENT, height, true);
        if (showAnimation) {
            mTalkPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
        }
        mTalkPopupWindow.setFocusable(false);
        mTalkPopupWindow.setOutsideTouchable(false);
        mTalkPopupWindow.showAsDropDown(mRealPlayPlayRl);
        mTalkPopupWindow.update();
        mTalkRingView.post(new Runnable() {
            @Override
            public void run() {
                if (mTalkRingView != null) {
                    mTalkRingView.setMinRadiusAndDistance(mTalkBackControlBtn.getHeight() / 2f,
                            Utils.dip2px(getContext(), 22));
                }
            }
        });
    }

    private void closeTalkPopupWindow(boolean stopTalk, boolean startAnim) {
        if (mTalkPopupWindow != null) {
            LogUtil.i(TAG, "closeTalkPopupWindow");
            dismissPopWindow(mTalkPopupWindow);
            mTalkPopupWindow = null;
        }
        mTalkRingView = null;
        if (stopTalk) {
            stopVoiceTalk(startAnim);
        }
    }

    /**
     * ????????????????????????
     *
     * @see
     * @since V1.8.3
     */
    private void openPtzPopupWindow(View parent) {
        closePtzPopupWindow();
        mIsOnPtz = true;
        setPlayScaleUI(1, null, null);
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_ptz_wnd, null, true);
        mPtzControlLy = (LinearLayout) layoutView.findViewById(R.id.ptz_control_ly);
        ImageButton ptzCloseBtn = (ImageButton) layoutView.findViewById(R.id.ptz_close_btn);
        ptzCloseBtn.setOnClickListener(mOnPopWndClickListener);
        ImageButton ptzTopBtn = (ImageButton) layoutView.findViewById(R.id.ptz_top_btn);
        ptzTopBtn.setOnTouchListener(mOnTouchListener);
        ImageButton ptzBottomBtn = (ImageButton) layoutView.findViewById(R.id.ptz_bottom_btn);
        ptzBottomBtn.setOnTouchListener(mOnTouchListener);
        ImageButton ptzLeftBtn = (ImageButton) layoutView.findViewById(R.id.ptz_left_btn);
        ptzLeftBtn.setOnTouchListener(mOnTouchListener);
        ImageButton ptzRightBtn = (ImageButton) layoutView.findViewById(R.id.ptz_right_btn);
        ptzRightBtn.setOnTouchListener(mOnTouchListener);
        ImageButton ptzFlipBtn = (ImageButton) layoutView.findViewById(R.id.ptz_flip_btn);
        ptzFlipBtn.setOnClickListener(mOnPopWndClickListener);
        ImageButton tx_zoomin = (ImageButton) layoutView.findViewById(R.id.tx_zoomin);
        tx_zoomin.setOnTouchListener(mOnTouchListener);
        ImageButton tx_zoomout = (ImageButton) layoutView.findViewById(R.id.tx_zoomout);
        tx_zoomout.setOnTouchListener(mOnTouchListener);
        tx_speed = (ImageView) layoutView.findViewById(R.id.tx_speed);
        tx_speed.setOnClickListener(mOnPopWndClickListener);
        int height = mLocalInfo.getScreenHeight() - mPortraitTitleBar.getHeight() - mRealPlayPlayRl.getHeight()
                - (mRealPlayRect != null ? mRealPlayRect.top : mLocalInfo.getNavigationBarHeight());
        mPtzPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.MATCH_PARENT, height, true);
        mPtzPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPtzPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
        mPtzPopupWindow.setFocusable(true);
        mPtzPopupWindow.setOutsideTouchable(true);
        mPtzPopupWindow.showAsDropDown(parent);
        mPtzPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                LogUtil.i(TAG, "KEYCODE_BACK DOWN");
                mPtzPopupWindow = null;
                mPtzControlLy = null;
                closePtzPopupWindow();
            }
        });
        mPtzPopupWindow.update();
    }

    private void closePtzPopupWindow() {
        mIsOnPtz = false;
        if (mPtzPopupWindow != null) {
            dismissPopWindow(mPtzPopupWindow);
            mPtzPopupWindow = null;
            mPtzControlLy = null;
            setForceOrientation(0);
        }
    }

    private void openQualityPopupWindow(View anchor) {
        if (mEZPlayer == null) {
            return;
        }
        closeQualityPopupWindow();
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_quality_items, null, true);

        Button qualityHdBtn = (Button) layoutView.findViewById(R.id.quality_hd_btn);
        qualityHdBtn.setOnClickListener(mOnPopWndClickListener);
        Button qualityBalancedBtn = (Button) layoutView.findViewById(R.id.quality_balanced_btn);
        qualityBalancedBtn.setOnClickListener(mOnPopWndClickListener);
        Button qualityFlunetBtn = (Button) layoutView.findViewById(R.id.quality_flunet_btn);
        qualityFlunetBtn.setOnClickListener(mOnPopWndClickListener);

        // ???????????????2-?????????1-?????????0-??????
        if (mCameraInfo.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET) {
            qualityFlunetBtn.setEnabled(false);
        } else if (mCameraInfo.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED) {
            qualityBalancedBtn.setEnabled(false);
        } else if (mCameraInfo.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_HD) {
            qualityHdBtn.setEnabled(false);
        }

        int height = 105;

        qualityFlunetBtn.setVisibility(View.VISIBLE);
        qualityBalancedBtn.setVisibility(View.VISIBLE);
        qualityHdBtn.setVisibility(View.VISIBLE);

        height = Utils.dip2px(getContext(), height);
        mQualityPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.WRAP_CONTENT, height, true);
        mQualityPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mQualityPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                LogUtil.i(TAG, "KEYCODE_BACK DOWN");
                mQualityPopupWindow = null;
                closeQualityPopupWindow();
            }
        });
        try {
            mQualityPopupWindow.showAsDropDown(anchor, -Utils.dip2px(getContext(), 5),
                    -(height + anchor.getHeight() + Utils.dip2px(getContext(), 8)));
        } catch (Exception e) {
            e.printStackTrace();
            closeQualityPopupWindow();
        }
    }

    private void closeQualityPopupWindow() {
        if (mQualityPopupWindow != null) {
            dismissPopWindow(mQualityPopupWindow);
            mQualityPopupWindow = null;
        }
    }

    /**
     * ????????????
     *
     * @see
     * @since V1.0
     */
    private void onRecordBtnClick() {
        mControlDisplaySec = 0;
        if (mIsRecording) {
            stopRealPlayRecord();
            return;
        }

        if (!SDCardUtil.isSDCardUseable()) {
            // ??????SD????????????
            Utils.showToast(getContext(), R.string.remoteplayback_SDCard_disable_use);
            return;
        }

        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
            // ??????????????????
            Utils.showToast(getContext(), R.string.remoteplayback_record_fail_for_memory);
            return;
        }

        if (mEZPlayer != null) {
            mCaptureDisplaySec = 4;
            updateCaptureUI();
            mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
            // ????????????deviceSerial+???????????????????????????demo??????????????????????????????
            java.util.Date date = new java.util.Date();
            String strRecordFile = DEFAULT_SAVE_VIDEO_PATH + mCameraInfo.getCameraName()+"/"
                    + String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) + ".mp4";
            //????????????
            List<VideoFilePath> files = DBManager.getInstance().get(VideoFilePath.class);
            if(files.size()>=200){
                DBManager.getInstance().delete(VideoFilePath.class,"path=",new String[]{files.get(0).getPath()});
            }
            String name = String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) +".jpg";
            DBManager.getInstance().insert(new VideoFilePath(strRecordFile, name));
            if (mEZPlayer.startLocalRecordWithFile(strRecordFile)) {
                handleRecordSuccess(strRecordFile);
            } else {
                handleRecordFail();
            }
        }
    }

    /**
     * ????????????
     *
     * @see
     * @since V1.0
     */
    private void stopRealPlayRecord() {
        if (mEZPlayer == null || !mIsRecording) {
            return;
        }
        Toast.makeText(getContext(), getResources().getString(R.string.already_saved_to_volume), Toast.LENGTH_SHORT).show();
        // ?????????????????????check??????
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayRecordContainer, mRealPlayRecordStartBtn,
                        mRealPlayRecordBtn, 0, 90);
            } else {
                mRealPlayRecordStartBtn.setVisibility(View.GONE);
                mRealPlayRecordBtn.setVisibility(View.VISIBLE);
            }
            mRealPlayFullRecordStartBtn.setVisibility(View.GONE);
            mRealPlayFullRecordBtn.setVisibility(View.VISIBLE);
        } else {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayFullRecordContainer, mRealPlayFullRecordStartBtn,
                        mRealPlayFullRecordBtn, 0, 90);
                mRecordRotateViewUtil_land.applyRotation(mRealPlayFullRecordContainer_land, mRealPlayFullRecordStartBtn_land,
                        mRealPlayFullRecordBtn_land, 0, 90);
            } else {
                mRealPlayFullRecordStartBtn.setVisibility(View.GONE);
                mRealPlayFullRecordStartBtn_land.setVisibility(View.GONE);
                mRealPlayFullRecordBtn.setVisibility(View.VISIBLE);
                mRealPlayFullRecordBtn_land.setVisibility(View.VISIBLE);

            }
            mRealPlayRecordStartBtn.setVisibility(View.GONE);
            mRealPlayRecordBtn.setVisibility(View.VISIBLE);
        }
        mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
        mEZPlayer.stopLocalRecord();
        // ?????????????????????
        mRealPlayRecordLy.setVisibility(View.GONE);
        mCaptureDisplaySec = 0;
        mIsRecording = false;
        updateCaptureUI();
    }
    /**
     * ????????????????????????
     *
     * @since V1.0
     */
    private void onCapturePicBtnClick() {
        java.util.Date date = new java.util.Date();
        String path = DEFAULT_SAVE_IMAGE_PATH +mCameraInfo.getCameraName()+"/"
                + String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) +".jpg";
        mControlDisplaySec = 0;
        if (!SDCardUtil.isSDCardUseable()) {
            // ??????SD????????????
            Utils.showToast(getContext(), R.string.remoteplayback_SDCard_disable_use);
            return;
        }
        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
            // ??????????????????
            Utils.showToast(getContext(), R.string.remoteplayback_capture_fail_for_memory);
            return;
        }

        if (mEZPlayer != null) {
            mCaptureDisplaySec = 4;
            updateCaptureUI();
            Thread thr = new Thread() {
                @Override
                public void run() {
                    Bitmap bmp = mEZPlayer.capturePicture();
                    if (bmp != null) {
                        try {
                            mAudioPlayUtil.playAudioFile(AudioPlayUtil.CAPTURE_SOUND);
                            if (TextUtils.isEmpty(path)) {
                                bmp.recycle();
                                bmp = null;
                                return;
                            }
                            EZUtils.saveCapturePictrue(path, bmp);
                            //????????????
                            List<PicFilePath> files = DBManager.getInstance().get(PicFilePath.class);
                            if (files.size()>=200){
                                DBManager.getInstance().delete(PicFilePath.class, "path=?", new String[]{files.get(0).getPath()});
                            }
                            String name = String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) +".jpg";
                            DBManager.getInstance().insert(new PicFilePath(path, name));
                            MediaScanner mMediaScanner = new MediaScanner(getContext());
                            mMediaScanner.scanFile(path, "jpg");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), getResources().getString(R.string.already_saved_to_volume), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (InnerException e) {
                            e.printStackTrace();
                        } finally {
                            if (bmp != null) {
                                bmp.recycle();
                                bmp = null;
                                return;
                            }
                        }
                    }
                    super.run();
                }
            };
            thr.start();
        }
    }

    private void onRealPlaySvClick() {
        if (mCameraInfo != null && mEZPlayer != null && mDeviceInfo != null) {
            if (mDeviceInfo.getStatus() != 1) {
                return;
            }
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                setRealPlayControlRlVisibility();
            } else {
                setRealPlayFullOperateBarVisibility();
            }
        } else if (mRtspUrl != null) {
            setRealPlayControlRlVisibility();
        }
    }

    private void setRealPlayControlRlVisibility() {
        if (mLandscapeTitleBar.getVisibility() == View.VISIBLE || mRealPlayControlRl.getVisibility() == View.VISIBLE) {
            //            mRealPlayControlRl.setVisibility(View.GONE);
            mLandscapeTitleBar.setVisibility(View.GONE);
            mRealPlayFullOperateBar.setVisibility(View.GONE);
            mRead_ptz_wnd_landscape.setVisibility(View.GONE);
            closeQualityPopupWindow();
        } else {
            mRealPlayControlRl.setVisibility(View.VISIBLE);
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!mIsOnTalk && !mIsOnPtz) {
                    mLandscapeTitleBar.setVisibility(View.VISIBLE);
                    mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
                }
            } else {
                mLandscapeTitleBar.setVisibility(View.GONE);
                mRealPlayFullOperateBar.setVisibility(View.GONE);
                mRead_ptz_wnd_landscape.setVisibility(View.GONE);
            }
            mControlDisplaySec = 0;
        }
    }

    private void setRealPlayFullOperateBarVisibility() {
        if (mLandscapeTitleBar.getVisibility() == View.VISIBLE) {
            mRealPlayFullOperateBar.setVisibility(View.GONE);
            if (!mIsOnTalk && !mIsOnPtz) {
                mFullscreenFullButton.setVisibility(View.GONE);
                mRealPlayFullOperateBar.setVisibility(View.GONE);
            }
            mLandscapeTitleBar.setVisibility(View.GONE);
        } else {
            if (!mIsOnTalk && !mIsOnPtz) {
                mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
                mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
            }
            mControlDisplaySec = 0;
        }
    }

    /**
     * ????????????
     *
     * @see
     * @since V2.0
     */
    private void startRealPlay() {
        // ???????????????????????????????????????
        LogUtil.d(TAG, "startRealPlay");
        if (mStatus == RealPlayStatus.STATUS_START || mStatus == RealPlayStatus.STATUS_PLAY) {
            return;
        }
        // ????????????????????????
        if (!ConnectionDetector.isNetworkAvailable(getContext())) {
            // ????????????????????????
            setRealPlayFailUI(getString(R.string.realplay_play_fail_becauseof_network));
            return;
        }
        mStatus = RealPlayStatus.STATUS_START;
        setRealPlayLoadingUI();

        if (mCameraInfo != null) {
            if (mEZPlayer == null) {
                mEZPlayer = getOpenSDK().createPlayer(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
            }

            if (mEZPlayer == null)
                return;
            if (mDeviceInfo == null) {
                return;
            }
            if (mDeviceInfo.getIsEncrypt() == 1) {
                //mEZPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
                //?????????????????????
                String name = mCameraInfo.getDeviceSerial()+ String.valueOf(mCameraInfo.getCameraNo());
                List<Verifcode> verifcodeList =  DBManager.getInstance().get(Verifcode.class);
                if (verifcodeList.size() != 0){
                    for (Verifcode verifcode : verifcodeList){
                        if (verifcode.getName().equals(name)){
                            mVerifyCode = verifcode.getCode();
                        }
                    }
                }
                mEZPlayer.setPlayVerifyCode(mVerifyCode);
            }
            mEZPlayer.setHandler(mHandler);
            mEZPlayer.setSurfaceHold(mRealPlaySh);
            mEZPlayer.startRealPlay();
        } else if (mRtspUrl != null) {
            mEZPlayer = getOpenSDK().createPlayerWithUrl(mRtspUrl);
            if (mEZPlayer == null) {
                return;
            }
            mEZPlayer.setHandler(mHandler);
            mEZPlayer.setSurfaceHold(mRealPlaySh);

            mEZPlayer.startRealPlay();
        }
        updateLoadingProgress(0);
    }

    /**
     * ????????????
     *
     * @see
     * @since V1.0
     */
    private void stopRealPlay() {
        LogUtil.d(TAG, "stopRealPlay");
        mStatus = RealPlayStatus.STATUS_STOP;

        stopUpdateTimer();
        if (mEZPlayer != null) {
            stopRealPlayRecord();

            mEZPlayer.stopRealPlay();
        }
        mStreamFlow = 0;
    }

    private void setRealPlayLoadingUI() {
        mRealPlaySv.setVisibility(View.INVISIBLE);
        mRealPlaySv.setVisibility(View.VISIBLE);
        setStartloading();
        mRealPlayBtn.setBackgroundResource(R.drawable.play_stop_selector);
        if (mCameraInfo != null && mDeviceInfo != null) {
            mRealPlayCaptureBtn.setEnabled(false);
            mRealPlayRecordBtn.setEnabled(false);
            if (mDeviceInfo.getStatus() == 1) {
                mRealPlayQualityBtn.setEnabled(true);
            } else {
                mRealPlayQualityBtn.setEnabled(false);
            }
            mRealPlayPtzBtn.setEnabled(false);
            mRealPlayFullPlayBtn.setBackgroundResource(R.drawable.play_full_stop_selector);
            mRealPlayFullCaptureBtn.setEnabled(false);
            mRealPlayFullRecordBtn.setEnabled(false);
            mRealPlayFullRecordBtn_land.setEnabled(false);
            mRealPlayFullFlowLy.setVisibility(View.GONE);
            mRealPlayFullPtzBtn.setEnabled(false);
        }
        showControlRlAndFullOperateBar();
    }

    private void showControlRlAndFullOperateBar() {
        if (mRtspUrl != null || mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mRealPlayControlRl.setVisibility(View.VISIBLE);
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!mIsOnTalk && !mIsOnPtz) {
                    mLandscapeTitleBar.setVisibility(View.VISIBLE);
                }
            } else {
                mLandscapeTitleBar.setVisibility(View.GONE);
            }
            mControlDisplaySec = 0;
        } else {
            if (!mIsOnTalk && !mIsOnPtz) {
                mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
                //                mFullscreenFullButton.setVisibility(View.VISIBLE);
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
            mControlDisplaySec = 0;
        }
    }

    private void setRealPlayStopUI() {
        stopUpdateTimer();
        updateOrientation();
        setRealPlaySvLayout();
        setStopLoading();
        hideControlRlAndFullOperateBar(true);
        mRealPlayBtn.setBackgroundResource(R.drawable.play_play_selector);
        if (mCameraInfo != null && mDeviceInfo != null) {
            closePtzPopupWindow();
            setFullPtzStopUI(false);
            mRealPlayCaptureBtn.setEnabled(false);
            mRealPlayRecordBtn.setEnabled(false);
            if (mDeviceInfo.getStatus() == 1) {
                mRealPlayQualityBtn.setEnabled(true);
            } else {
                mRealPlayQualityBtn.setEnabled(false);
            }
            mRealPlayFullPtzBtn.setEnabled(false);
            if (mDeviceInfo.getStatus() == 1) {
                mRealPlayPrivacyBtn.setEnabled(true);
                mRealPlaySslBtn.setEnabled(true);
            } else {
                mRealPlayPrivacyBtn.setEnabled(false);
                mRealPlaySslBtn.setEnabled(false);
            }
            mRealPlayFullPlayBtn.setBackgroundResource(R.drawable.play_full_play_selector);
            mRealPlayFullCaptureBtn.setEnabled(false);
            mRealPlayFullRecordBtn.setEnabled(false);
            mRealPlayFullRecordBtn_land.setEnabled(false);
            mRealPlayPtzBtn.setEnabled(false);
        }
    }

    private void setRealPlayFailUI(String errorStr) {
        showType();
        stopUpdateTimer();
        updateOrientation();
        {
            setLoadingFail(errorStr);
        }
        mRealPlayFullFlowLy.setVisibility(View.GONE);
        mRealPlayBtn.setBackgroundResource(R.drawable.play_play_selector);
        hideControlRlAndFullOperateBar(true);
        if (mCameraInfo != null && mDeviceInfo != null) {
            closePtzPopupWindow();
            setFullPtzStopUI(false);
            mRealPlayCaptureBtn.setEnabled(false);
            mRealPlayRecordBtn.setEnabled(false);
            if (mDeviceInfo.getStatus() == 1 && (mEZPlayer == null)) {
                mRealPlayQualityBtn.setEnabled(true);
            } else {
                mRealPlayQualityBtn.setEnabled(false);
            }
            mRealPlayPtzBtn.setEnabled(false);
            if (mDeviceInfo.getStatus() == 1) {
                mRealPlayPrivacyBtn.setEnabled(true);
                mRealPlaySslBtn.setEnabled(true);
            } else {
                mRealPlayPrivacyBtn.setEnabled(false);
                mRealPlaySslBtn.setEnabled(false);
            }
            mRealPlayFullPlayBtn.setBackgroundResource(R.drawable.play_full_play_selector);
            mRealPlayFullCaptureBtn.setEnabled(false);
            mRealPlayFullRecordBtn.setEnabled(false);
            mRealPlayFullRecordBtn_land.setEnabled(false);
            mRealPlayFullPtzBtn.setEnabled(false);
        }
    }

    private void setRealPlaySuccessUI() {
        showType();
        updateOrientation();
        setLoadingSuccess();
        mRealPlayFlowTv.setVisibility(View.VISIBLE);
        mRealPlayFullFlowLy.setVisibility(View.GONE);
        mRealPlayBtn.setBackgroundResource(R.drawable.play_stop_selector);
        if (mCameraInfo != null && mDeviceInfo != null) {
            mRealPlayCaptureBtn.setEnabled(true);
            mRealPlayRecordBtn.setEnabled(true);
            if (mDeviceInfo.getStatus() == 1) {
                mRealPlayQualityBtn.setEnabled(true);
            } else {
                mRealPlayQualityBtn.setEnabled(false);
            }
            if (getSupportPtz() == 1) {
                mRealPlayPtzBtn.setEnabled(true);
            }

            mRealPlayFullPlayBtn.setBackgroundResource(R.drawable.play_full_stop_selector);
            mRealPlayFullCaptureBtn.setEnabled(true);
            mRealPlayFullRecordBtn.setEnabled(true);
            mRealPlayFullRecordBtn_land.setEnabled(true);
            mRealPlayFullPtzBtn.setEnabled(true);
        }
        startUpdateTimer();
    }

    /**
     * ??????????????????
     *
     * @see
     * @since V1.0
     */
    private void checkRealPlayFlow() {
        if ((mEZPlayer != null && mRealPlayFlowTv.getVisibility() == View.VISIBLE)) {
            // ??????????????????
            long streamFlow = mEZPlayer.getStreamFlow();
            updateRealPlayFlowTv(streamFlow);
        }
    }

    private void updateRealPlayFlowTv(long streamFlow) {
        long streamFlowUnit = streamFlow - mStreamFlow;
        if (streamFlowUnit < 0) {
            streamFlowUnit = 0;
        }
        float fKBUnit = (float) streamFlowUnit / (float) Constant.KB;
        String descUnit = String.format("%.2f k/s ", fKBUnit);
        // ????????????
        mRealPlayFlowTv.setText(descUnit);
        mStreamFlow = streamFlow;
    }

    private void setOrientation(int sensor) {
        if (mForceOrientation != 0) {
            LogUtil.d(TAG, "setOrientation mForceOrientation:" + mForceOrientation);
            return;
        }
        if (sensor == ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
            mScreenOrientationHelper.disableSensorOrientation();
        } else {
            mScreenOrientationHelper.disableSensorOrientation();
        }
    }

    public void setForceOrientation(int orientation) {
        if (mForceOrientation == orientation) {
            LogUtil.d(TAG, "setForceOrientation no change");
            return;
        }
        mForceOrientation = orientation;
        if (mForceOrientation != 0) {
            if (mForceOrientation != mOrientation) {
                if (mForceOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    mScreenOrientationHelper.portrait();
                } else {
                    mScreenOrientationHelper.landscape();
                }
            }
            mScreenOrientationHelper.disableSensorOrientation();
        } else {
            updateOrientation();
        }
    }
    @SuppressLint("NewApi")
    @Override
    public boolean handleMessage(Message msg) {
//        if (this.isFinishing()) {
//            return false;
//        }
        switch (msg.what) {
            case EZConstants.EZRealPlayConstants.MSG_GET_CAMERA_INFO_SUCCESS:
                updateLoadingProgress(20);
                handleGetCameraInfoSuccess();
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_START:
                updateLoadingProgress(40);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_CONNECTION_START:
                updateLoadingProgress(60);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_CONNECTION_SUCCESS:
                updateLoadingProgress(80);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
                handlePlaySuccess(msg);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
                handlePlayFail(msg.obj);
                break;
            case EZConstants.EZRealPlayConstants.MSG_SET_VEDIOMODE_SUCCESS:
                handleSetVedioModeSuccess();
                break;
            case EZConstants.EZRealPlayConstants.MSG_SET_VEDIOMODE_FAIL:
                handleSetVedioModeFail(msg.arg1);
                break;
            case EZConstants.EZRealPlayConstants.MSG_PTZ_SET_FAIL:
                handlePtzControlFail(msg);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_VOICETALK_SUCCESS:
                handleVoiceTalkSucceed();
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_VOICETALK_STOP:
                handleVoiceTalkStoped(false);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_VOICETALK_FAIL:
                ErrorInfo errorInfo = (ErrorInfo) msg.obj;
                handleVoiceTalkFailed(errorInfo);
                break;
            case MSG_PLAY_UI_UPDATE:
                updateRealPlayUI();
                break;
            case MSG_AUTO_START_PLAY:
                startRealPlay();
                break;
            case MSG_CLOSE_PTZ_PROMPT:
                mRealPlayFullPtzPromptIv.setVisibility(View.GONE);
                break;
            case MSG_HIDE_PTZ_DIRECTION:
                handleHidePtzDirection(msg);
                break;
            case MSG_HIDE_PAGE_ANIM:
                hidePageAnim();
                break;
            case MSG_PLAY_UI_REFRESH:
                initUI();
                break;
            case MSG_PREVIEW_START_PLAY:
                mPageAnimIv.setVisibility(View.GONE);
                mRealPlayPreviewTv.setVisibility(View.GONE);
                mStatus = RealPlayStatus.STATUS_INIT;
                startRealPlay();
                break;
            default:
                break;
        }
        return false;
    }

    private void handleHidePtzDirection(Message msg) {
        if (mHandler == null) {
            return;
        }
        mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
        if (msg.arg1 > 2) {
            mRealPlayPtzDirectionIv.setVisibility(View.GONE);
        } else {
            mRealPlayPtzDirectionIv.setVisibility(msg.arg1 == 1 ? View.GONE : View.VISIBLE);
            Message message = new Message();
            message.what = MSG_HIDE_PTZ_DIRECTION;
            message.arg1 = msg.arg1 + 1;
            mHandler.sendMessageDelayed(message, 500);
        }
    }

    private void handlePtzControlFail(Message msg) {
        LogUtil.d(TAG, "handlePtzControlFail:" + msg.arg1);
        switch (msg.arg1) {
            case ErrorCode.ERROR_CAS_PTZ_CONTROL_CALLING_PRESET_FAILED:// ??????????????????????????????????????????
                Utils.showToast(getContext(), R.string.camera_lens_too_busy, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_PRESETING_FAILE:// ???????????????????????????
                Utils.showToast(getContext(), R.string.ptz_is_preseting, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_CONTROL_TIMEOUT_SOUND_LACALIZATION_FAILED:// ????????????????????????
                break;
            case ErrorCode.ERROR_CAS_PTZ_CONTROL_TIMEOUT_CRUISE_TRACK_FAILED:// ??????????????????(????????????????????????)
                Utils.showToast(getContext(), R.string.ptz_control_timeout_cruise_track_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_INVALID_POSITION_FAILED:// ???????????????????????????
                Utils.showToast(getContext(), R.string.ptz_preset_invalid_position_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_CURRENT_POSITION_FAILED:// ??????????????????????????????
                Utils.showToast(getContext(), R.string.ptz_preset_current_position_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_SOUND_LOCALIZATION_FAILED:// ????????????????????????????????????
                Utils.showToast(getContext(), R.string.ptz_preset_sound_localization_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_OPENING_PRIVACY_FAILED:// ??????????????????????????????
            case ErrorCode.ERROR_CAS_PTZ_CLOSING_PRIVACY_FAILED:// ??????????????????????????????
            case ErrorCode.ERROR_CAS_PTZ_MIRRORING_FAILED:// ?????????????????????????????????????????????????????????????????????????????????
                Utils.showToast(getContext(), R.string.ptz_operation_too_frequently, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_CONTROLING_FAILED:// ??????????????????????????????????????????(?????????????????????????????????????????????????????????????????????)
                break;
            case ErrorCode.ERROR_CAS_PTZ_FAILED:// ????????????????????????
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_EXCEED_MAXNUM_FAILED:// ?????????????????????????????????
                Utils.showToast(getContext(), R.string.ptz_preset_exceed_maxnum_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRIVACYING_FAILED:// ??????????????????????????????????????????????????????????????????????????????
                Utils.showToast(getContext(), R.string.ptz_privacying_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_TTSING_FAILED:// ??????????????????????????????(???????????????????????????????????????????????????????????????
                Utils.showToast(getContext(), R.string.ptz_mirroring_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_UP_LIMIT_FAILED:// ?????????????????????????????????
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_DOWN_LIMIT_FAILED:// ?????????????????????????????????
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_LEFT_LIMIT_FAILED:// ?????????????????????????????????
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_RIGHT_LIMIT_FAILED:// ?????????????????????????????????
                setPtzDirectionIv(-1, msg.arg1);
                break;
            default:
                Utils.showToast(getContext(), R.string.ptz_operation_failed, msg.arg1);
                break;
        }
    }

    private void hidePageAnim() {
        mHandler.removeMessages(MSG_HIDE_PAGE_ANIM);
        if (mPageAnimDrawable != null) {
            if (mPageAnimDrawable.isRunning()) {
                mPageAnimDrawable.stop();
            }
            mPageAnimDrawable = null;
            mPageAnimIv.setBackgroundDrawable(null);
            mPageAnimIv.setVisibility(View.GONE);
        }
        if (mPageAnimIv != null) {
            mPageAnimIv.setBackgroundDrawable(null);
            mPageAnimIv.setVisibility(View.GONE);
        }
    }

    private void setRealPlayTalkUI() {
        if (mEZPlayer != null && mDeviceInfo != null && (mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport)) {
            mRealPlayTalkBtnLy.setVisibility(View.VISIBLE);
            if (mCameraInfo != null && mDeviceInfo.getStatus() == 1) {
                mRealPlayTalkBtn.setEnabled(true);
            } else {
                mRealPlayTalkBtn.setEnabled(false);
            }
            if (mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport) {
            } else {
                mRealPlayFullTalkBtn.setVisibility(View.GONE);
            }
        } else {
            mRealPlayTalkBtnLy.setVisibility(View.GONE);
            mRealPlayFullTalkBtn.setVisibility(View.GONE);
        }
        mRealPlayTalkBtnLy.setVisibility(View.VISIBLE);
    }

    private void updatePermissionUI() {
        mRealPlayTalkBtnLy.setVisibility(View.VISIBLE);
    }

    private void updateUI() {
        //?????????????????????
        setRealPlayTalkUI();
        setVideoLevel();
        {
            mRealPlaySslBtnLy.setVisibility(View.GONE);
        }
        if (getSupportPtz() == 1) {
            mRealPlayPtzBtnLy.setVisibility(View.VISIBLE);
            mRealPlayFullPtzBtn.setVisibility(View.VISIBLE);
        } else {
            mRealPlayPtzBtnLy.setEnabled(false);
            mRealPlayFullPtzBtn.setEnabled(false);
        }

        updatePermissionUI();
    }

    /**
     * ????????????????????????
     *
     * @see
     * @since V1.0
     */
    private void handleGetCameraInfoSuccess() {
        LogUtil.i(TAG, "handleGetCameraInfoSuccess");
        //?????????????????????
        updateUI();

    }

    private void handleVoiceTalkSucceed() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            openTalkPopupWindow(true);
        } else {
            mRealPlayFullTalkAnimBtn.setVisibility(View.VISIBLE);
            //            mFullscreenFullButton.setVisibility(View.VISIBLE);
            ((AnimationDrawable) mRealPlayFullTalkAnimBtn.getBackground()).start();
        }

        mRealPlayTalkBtn.setEnabled(true);
        mRealPlayFullTalkBtn.setEnabled(true);
        mRealPlayFullTalkAnimBtn.setEnabled(true);
    }

    private void handleVoiceTalkFailed(ErrorInfo errorInfo) {
        LogUtil.d(TAG, "Talkback failed. " + errorInfo.toString());
        closeTalkPopupWindow(true, false);
        switch (errorInfo.errorCode) {
            case ErrorCode.ERROR_TRANSF_DEVICE_TALKING:
                Utils.showToast(getContext(), R.string.realplay_play_talkback_fail_ison);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_PRIVACYON:
                Utils.showToast(getContext(), R.string.realplay_play_talkback_fail_privacy);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_OFFLINE:
                Utils.showToast(getContext(), R.string.realplay_fail_device_not_exist);
                break;
            case ErrorCode.ERROR_TTS_MSG_REQ_TIMEOUT:
            case ErrorCode.ERROR_TTS_MSG_SVR_HANDLE_TIMEOUT:
            case ErrorCode.ERROR_TTS_WAIT_TIMEOUT:
            case ErrorCode.ERROR_TTS_HNADLE_TIMEOUT:
                Utils.showToast(getContext(), R.string.realplay_play_talkback_request_timeout, errorInfo.errorCode);
                break;
            case ErrorCode.ERROR_CAS_AUDIO_SOCKET_ERROR:
            case ErrorCode.ERROR_CAS_AUDIO_RECV_ERROR:
            case ErrorCode.ERROR_CAS_AUDIO_SEND_ERROR:
                Utils.showToast(getContext(), R.string.realplay_play_talkback_network_exception, errorInfo.errorCode);
                break;
            default:
                Utils.showToast(getContext(), R.string.realplay_play_talkback_fail, errorInfo.errorCode);
                break;
        }
    }

    private void handleVoiceTalkStoped(boolean startAnim) {
        if (mIsOnTalk) {
            mIsOnTalk = false;
            setForceOrientation(0);
        }
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (startAnim) {
                mRealPlayFullTalkAnimBtn.setVisibility(View.GONE);
                mFullscreenFullButton.setVisibility(View.GONE);
                mRealPlayFullAnimBtn.setBackgroundResource(R.drawable.speech_1);
                startFullBtnAnim(mRealPlayFullAnimBtn, mEndXy, mStartXy, new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mRealPlayFullAnimBtn.setVisibility(View.GONE);
                        onRealPlaySvClick();
                    }
                });
            } else {
                mRealPlayFullTalkAnimBtn.setVisibility(View.GONE);
                mFullscreenFullButton.setVisibility(View.GONE);
            }
        }

        mRealPlayTalkBtn.setEnabled(true);
        mRealPlayFullTalkBtn.setEnabled(true);
        mRealPlayFullTalkAnimBtn.setEnabled(true);

        if (mStatus == RealPlayStatus.STATUS_PLAY) {
            if (mEZPlayer != null) {
                if (mLocalInfo.isSoundOpen()) {
                    mEZPlayer.openSound();
                } else {
                    mEZPlayer.closeSound();
                }
            }
        }
    }

    private void handleSetVedioModeSuccess() {
        closeQualityPopupWindow();
        setVideoLevel();
        try {
            dismissLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mStatus == RealPlayStatus.STATUS_PLAY) {
            // ????????????
            stopRealPlay();
            //??????????????????stopRealPlay??????????????????surface, startRealPlay????????????????????????surface
            //??????????????????500ms
            SystemClock.sleep(500);
            // ????????????
            startRealPlay();
        }
    }

    private void handleSetVedioModeFail(int errorCode) {
        closeQualityPopupWindow();
        setVideoLevel();
        try {
            dismissLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.showToast(getContext(), R.string.realplay_set_vediomode_fail, errorCode);
    }

    /**
     * ??????????????????
     *
     * @param recordFilePath
     * @see
     * @since V2.0
     */
    private void handleRecordSuccess(String recordFilePath) {
        if (mCameraInfo == null) {
            return;
        }
        // ?????????????????????check??????
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayRecordContainer, mRealPlayRecordBtn,
                        mRealPlayRecordStartBtn, 0, 90);
            } else {
                mRealPlayRecordBtn.setVisibility(View.GONE);
                mRealPlayRecordStartBtn.setVisibility(View.VISIBLE);
            }
            mRealPlayFullRecordBtn.setVisibility(View.GONE);
            mRealPlayFullRecordStartBtn.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG,"mIsOnStop ="+mIsOnStop);
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayFullRecordContainer, mRealPlayFullRecordBtn,
                        mRealPlayFullRecordStartBtn, 0, 90);
                mRecordRotateViewUtil_land.applyRotation(mRealPlayFullRecordContainer_land, mRealPlayFullRecordBtn_land,
                        mRealPlayFullRecordStartBtn_land, 0, 90);
            } else {
                mRealPlayFullRecordBtn.setVisibility(View.GONE);
                mRealPlayFullRecordBtn_land.setVisibility(View.GONE);
                mRealPlayFullRecordStartBtn.setVisibility(View.VISIBLE);
                mRealPlayFullRecordStartBtn_land.setVisibility(View.VISIBLE);
            }
            mRealPlayRecordBtn.setVisibility(View.GONE);
            mRealPlayRecordStartBtn.setVisibility(View.VISIBLE);
        }
        mIsRecording = true;
        // ??????????????????
        mRealPlayRecordLy.setVisibility(View.VISIBLE);
        mRealPlayRecordTv.setText("00:00");
        mRecordSecond = 0;
    }

    private void handleRecordFail() {
        Utils.showToast(getContext(), R.string.remoteplayback_record_fail);
        if (mIsRecording) {
            stopRealPlayRecord();
        }
    }

    private void hideControlRlAndFullOperateBar(boolean excludeLandscapeTitle) {
        closeQualityPopupWindow();
        if (mRealPlayFullOperateBar != null) {
            mRealPlayFullOperateBar.setVisibility(View.GONE);
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                mFullscreenFullButton.setVisibility(View.GONE);
            } else {
                if (!mIsOnTalk && !mIsOnPtz) {
                    mFullscreenFullButton.setVisibility(View.GONE);
                }
            }
        }
        if (excludeLandscapeTitle && mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!mIsOnTalk && !mIsOnPtz) {
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
                mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
            }
        } else {
            mLandscapeTitleBar.setVisibility(View.GONE);
            mRealPlayFullOperateBar.setVisibility(View.GONE);
        }
    }

    private void updateRealPlayUI() {
        if (mControlDisplaySec == 5) {
            mControlDisplaySec = 0;
            hideControlRlAndFullOperateBar(false);
        }
        checkRealPlayFlow();
        updateCaptureUI();
        if (mIsRecording) {
            updateRecordTime();
        }
    }

    private void initCaptureUI() {
        mCaptureDisplaySec = 0;
        mRealPlayCaptureRl.setVisibility(View.GONE);
        mRealPlayCaptureIv.setImageURI(null);
        mRealPlayCaptureWatermarkIv.setTag(null);
        mRealPlayCaptureWatermarkIv.setVisibility(View.GONE);
    }

    // ????????????/????????????UI
    private void updateCaptureUI() {
        if (mRealPlayCaptureRl.getVisibility() == View.VISIBLE) {
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                if (mRealPlayControlRl.getVisibility() == View.VISIBLE) {
                    mRealPlayCaptureRlLp.setMargins(0, 0, 0, Utils.dip2px(getContext(), 40));
                } else {
                    mRealPlayCaptureRlLp.setMargins(0, 0, 0, 0);
                }
                mRealPlayCaptureRl.setLayoutParams(mRealPlayCaptureRlLp);
            } else {
                RelativeLayout.LayoutParams realPlayCaptureRlLp = new RelativeLayout.LayoutParams(
                        Utils.dip2px(getContext(), 65), Utils.dip2px(getContext(), 45));
                realPlayCaptureRlLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                realPlayCaptureRlLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                mRealPlayCaptureRl.setLayoutParams(realPlayCaptureRlLp);
            }
            if (mRealPlayCaptureWatermarkIv.getTag() != null) {
                mRealPlayCaptureWatermarkIv.setVisibility(View.VISIBLE);
                mRealPlayCaptureWatermarkIv.setTag(null);
            }
        }
        if (mCaptureDisplaySec >= 4) {
            initCaptureUI();
        }
    }

    /**
     * ??????????????????
     *
     * @see
     * @since V1.0
     */
    private void updateRecordTime() {
        if (mRealPlayRecordIv.getVisibility() == View.VISIBLE) {
            mRealPlayRecordIv.setVisibility(View.INVISIBLE);
        } else {
            mRealPlayRecordIv.setVisibility(View.VISIBLE);
        }
        // ????????????
        int leftSecond = mRecordSecond % 3600;
        int minitue = leftSecond / 60;
        int second = leftSecond % 60;
        // ??????????????????
        String recordTime = String.format("%02d:%02d", minitue, second);
        mRealPlayRecordTv.setText(recordTime);
    }

    // ??????????????????
    private void handlePasswordError(int title_resid, int msg1_resid, int msg2_resid) {
        stopRealPlay();
        setRealPlayStopUI();
        LogUtil.d(TAG, "startRealPlay");
        if (mCameraInfo == null || mStatus == RealPlayStatus.STATUS_START || mStatus == RealPlayStatus.STATUS_PLAY) {
            return;
        }
        // ????????????????????????
        if (!ConnectionDetector.isNetworkAvailable(getContext())) {
            // ????????????????????????
            setRealPlayFailUI(getString(R.string.realplay_play_fail_becauseof_network));
            return;
        }
        mStatus = RealPlayStatus.STATUS_START;
        setRealPlayLoadingUI();
        updateLoadingProgress(0);
    }

    /**
     * ???????????????????????????
     *
     * @see
     * @since V1.0
     */
    private void handlePlaySuccess(Message msg) {
        LogUtil.d(TAG, "handlePlaySuccess");
        mStatus = RealPlayStatus.STATUS_PLAY;
        // ????????????
        setRealPlaySound();
        mRealRatio = Constant.LIVE_VIEW_RATIO;
        boolean bSupport = true;
        if (bSupport) {
            initOperateBarUI(mRealRatio <= Constant.LIVE_VIEW_RATIO);
            initUI();
            if (mRealRatio <= Constant.LIVE_VIEW_RATIO) {
                setBigScreenOperateBtnLayout();
            }
        }
        setRealPlaySvLayout();
        setRealPlaySuccessUI();
        updatePtzUI();
        updateTalkUI();
        if (mDeviceInfo != null && mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport) {
            mRealPlayTalkBtn.setEnabled(true);
        } else {
            mRealPlayTalkBtn.setEnabled(false);
        }
        if (mEZPlayer != null) {
            mStreamFlow = mEZPlayer.getStreamFlow();
        }
    }

    private void setRealPlaySvLayout() {
        // ????????????????????????
        final int screenWidth = mLocalInfo.getScreenWidth();
        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ? (mLocalInfo.getScreenHeight() - mLocalInfo
                .getNavigationBarHeight()) : mLocalInfo.getScreenHeight();
        final RelativeLayout.LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation,
                mLocalInfo.getScreenWidth(), (int) (mLocalInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO),
                screenWidth, screenHeight);
        RelativeLayout.LayoutParams loadingR1Lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, realPlaySvlp.height);
        loadingR1Lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRealPlayLoadingRl.setLayoutParams(loadingR1Lp);
        mRealPlayPromptRl.setLayoutParams(loadingR1Lp);
        RelativeLayout.LayoutParams svLp = new RelativeLayout.LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        svLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRealPlaySv.setLayoutParams(svLp);
        if (mRtspUrl == null) {
            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            realPlayPlayRlLp.gravity = Gravity.CENTER;
            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
        } else {
            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            realPlayPlayRlLp.gravity = Gravity.CENTER;
            realPlayPlayRlLp.weight = 1;
            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
        }
        mRealPlayTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);
        setPlayScaleUI(1, null, null);
    }

    /**
     * ???????????????????????????
     *
     * @param obj - ?????????
     * @see
     * @since V1.0
     */
    private void handlePlayFail(Object obj) {
        int errorCode = 0;
        if (obj != null) {
            ErrorInfo errorInfo = (ErrorInfo) obj;
            errorCode = errorInfo.errorCode;
            LogUtil.d(TAG, "handlePlayFail:" + errorInfo.errorCode);
            hidePageAnim();
            stopRealPlay();
            updateRealPlayFailUI(errorInfo);
        }
    }

    private void updateRealPlayFailUI(ErrorInfo errorInfo) {
        int errorCode = errorInfo.errorCode;
        String txt = null;
        LogUtil.i(TAG, "updateRealPlayFailUI: errorCode:" + errorCode);
        // ????????????????????????
        switch (errorCode) {
            case ErrorCode.ERROR_TRANSF_ACCESSTOKEN_ERROR:
                ActivityUtils.goToLoginAgain(getActivity());
                return;
            case ErrorCode.ERROR_CAS_MSG_PU_NO_RESOURCE:
                txt = getString(R.string.remoteplayback_over_link);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_OFFLINE:
                if (mCameraInfo != null) {
                    mCameraInfo.setIsShared(0);
                }
                txt = getString(R.string.realplay_fail_device_not_exist);
                break;
            case ErrorCode.ERROR_INNER_STREAM_TIMEOUT:
                txt = getString(R.string.realplay_fail_connect_device);
                break;
            case ErrorCode.ERROR_WEB_CODE_ERROR:
                break;
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_OP_ERROR:
                break;
            case ErrorCode.ERROR_TRANSF_TERMINAL_BINDING:
                txt = "???????????????????????????????????????";
                break;
            // ????????????????????????????????????????????????????????????????????????????????????????????????
            case ErrorCode.ERROR_INNER_VERIFYCODE_NEED:
            case ErrorCode.ERROR_INNER_VERIFYCODE_ERROR: {
                VerifyCodeInput.VerifyCodeInputDialog(getContext(), this).show();
            }
            break;
            case ErrorCode.ERROR_EXTRA_SQUARE_NO_SHARING:
            default:
                if (errorCode!=0){
                    txt = errorInfo.description;
                }
                break;
        }

        if (!TextUtils.isEmpty(txt)) {
            setRealPlayFailUI(txt);
        } else {
            setRealPlayStopUI();
        }
    }


    /**
     * ???????????????
     *
     * @see
     * @since V1.0
     */
    private void startUpdateTimer() {
        stopUpdateTimer();
        // ??????????????????
        mUpdateTimer = new Timer();
        mUpdateTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mLandscapeTitleBar != null && mRealPlayControlRl != null
                        && (mLandscapeTitleBar.getVisibility() == View.VISIBLE || mRealPlayControlRl.getVisibility() == View.VISIBLE)
                        && mControlDisplaySec < 5) {
                    mControlDisplaySec++;
                }
                if (mRealPlayCaptureRl != null && mRealPlayCaptureRl.getVisibility() == View.VISIBLE
                        && mCaptureDisplaySec < 4) {
                    mCaptureDisplaySec++;
                }

                // ??????????????????
                if (mEZPlayer != null && mIsRecording) {
                    // ??????????????????
                    Calendar OSDTime = mEZPlayer.getOSDTime();
                    if (OSDTime != null) {
                        String playtime = Utils.OSD2Time(OSDTime);
                        if (!TextUtils.equals(playtime, mRecordTime)) {
                            mRecordSecond++;
                            mRecordTime = playtime;
                        }
                    }
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_PLAY_UI_UPDATE);
                }
            }
        };
        // ??????1000ms????????????1000ms????????????
        mUpdateTimer.schedule(mUpdateTimerTask, 0, 1000);
    }

    /**
     * ???????????????
     *
     * @see
     * @since V1.0
     */
    private void stopUpdateTimer() {
        mCaptureDisplaySec = 4;
        updateCaptureUI();
        mHandler.removeMessages(MSG_PLAY_UI_UPDATE);
        // ??????????????????
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
        if (mUpdateTimerTask != null) {
            mUpdateTimerTask.cancel();
            mUpdateTimerTask = null;
        }
    }

    private void dismissPopWindow(PopupWindow popupWindow) {
        if (popupWindow != null ) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    private void setPlayScaleUI(float scale, CustomRect oRect, CustomRect curRect) {
        if (scale == 1) {
            if (mPlayScale == scale) {
                return;
            }
            mRealPlayRatioTv.setVisibility(View.GONE);
            try {
                if (mEZPlayer != null) {
                    mEZPlayer.setDisplayRegion(false, null, null);
                }
            } catch (BaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            if (mPlayScale == scale) {
                try {
                    if (mEZPlayer != null) {
                        mEZPlayer.setDisplayRegion(true, oRect, curRect);
                    }
                } catch (BaseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return;
            }
            RelativeLayout.LayoutParams realPlayRatioTvLp = (RelativeLayout.LayoutParams) mRealPlayRatioTv
                    .getLayoutParams();
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                realPlayRatioTvLp.setMargins(Utils.dip2px(getContext(), 10), Utils.dip2px(getContext(), 10), 0, 0);
            } else {
                realPlayRatioTvLp.setMargins(Utils.dip2px(getContext(), 70), Utils.dip2px(getContext(), 20), 0, 0);
            }
            mRealPlayRatioTv.setLayoutParams(realPlayRatioTvLp);
            String sacleStr = String.valueOf(scale);
            mRealPlayRatioTv.setText(sacleStr.subSequence(0, Math.min(3, sacleStr.length())) + "X");
            mRealPlayRatioTv.setVisibility(View.GONE);
            hideControlRlAndFullOperateBar(false);
            try {
                if (mEZPlayer != null) {
                    mEZPlayer.setDisplayRegion(true, oRect, curRect);
                }
            } catch (BaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        mPlayScale = scale;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.realplay_pages_gallery:
                mRealPlayTouchListener.touch(v,event);
                break;
            case R.id.realplay_full_operate_bar:
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * ?????? ???????????????????????????
     *
     * @see
     * @since V1.8.2
     */
    private void showSharePasswordError() {
        // ????????????????????????
        handlePasswordError(R.string.realplay_encrypt_password_error_title, R.string.realplay_password_error_message4,
                0);
    }

    /**
     * ????????????????????????
     *
     * @see
     * @since V1.8
     */
    private void showType() {
        if (Config.LOGGING && mEZPlayer != null) {
        }
    }

    private void initLoadingUI() {
        // ???????????????????????????????????????
        mRealPlayPlayIv.setOnClickListener(this);
    }

    private void updateLoadingProgress(final int progress) {
        mRealPlayPlayLoading.setTag(Integer.valueOf(progress));
        mRealPlayPlayLoading.setText(progress + "%");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mRealPlayPlayLoading != null) {
                    Integer tag = (Integer) mRealPlayPlayLoading.getTag();
                    if (tag != null && tag.intValue() == progress) {
                        Random r = new Random();
                        mRealPlayPlayLoading.setText((progress + r.nextInt(20)) + "%");
                    }
                }
            }
        }, 500);
    }

    private void setStartloading() {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.VISIBLE);
        mRealPlayPlayIv.setVisibility(View.GONE);
        mRealPlayPlayPrivacyLy.setVisibility(View.GONE);
    }

    public void setStopLoading() {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.VISIBLE);
        mRealPlayPlayPrivacyLy.setVisibility(View.GONE);
    }

    public void setLoadingFail(String errorStr) {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setText(errorStr);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.GONE);
        mRealPlayPlayPrivacyLy.setVisibility(View.GONE);
    }

    private void setLoadingSuccess() {
        mRealPlayLoadingRl.setVisibility(View.INVISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.GONE);
    }

    @Override
    public void onInputVerifyCode(final String verifyCode) {
        String name = mCameraInfo.getDeviceSerial()+ String.valueOf(mCameraInfo.getCameraNo());
        if (mVerifyCode == null){
            DBManager.getInstance().insert(new Verifcode(name, verifyCode));
        }else{
            DBManager.getInstance().update(new Verifcode(name, verifyCode),"name=?", new String[]{name});
        }
        if (mEZPlayer != null) {
            startRealPlay();
        }
    }
    FileOutputStream mOs;
    /**
     * ??????????????????
     * @param var1:????????????
     * @param var2:????????????
     * @param var3:????????????
     * @param var4:???????????????
     */
    private EZOpenSDKListener.EZStandardFlowCallback mLocalRecordCb = new EZOpenSDKListener.EZStandardFlowCallback() {
        @Override
        public void onStandardFlowCallback(int type, byte[] data, int dataLen) {
            LogUtil.v(TAG, "standard flow. type is " + type + ". dataLen is " + dataLen + ". data0 is " + data[0]);
            if (mOs == null) {
                File f = new File("/sdcard/videogo.mp4");
                try {
                    mOs = new FileOutputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    LogUtil.e(TAG, "new record file failed");
                    return;
                }
            }
            try {
                mOs.write(data, 0, dataLen);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
        }
    };
}
