package com.suchness.landmanage.ui.fragment;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.suchness.deeplearningapp.app.base.BaseFragment;
import com.suchness.landmanage.R;
import com.suchness.landmanage.app.App;
import com.suchness.landmanage.app.utils.AudioPlayUtil;
import com.suchness.landmanage.app.utils.DataUtils;
import com.suchness.landmanage.app.utils.EZUtils;
import com.suchness.landmanage.app.utils.RemoteListUtil;
import com.suchness.landmanage.app.utils.VerifyCodeInput;
import com.suchness.landmanage.app.weight.loadtextview.LoadingTextView;
import com.suchness.landmanage.app.weight.loadtextview.LoadingView;
import com.suchness.landmanage.app.weight.playback.RemoteListContant;
import com.suchness.landmanage.app.weight.playback.WaitDialog;
import com.suchness.landmanage.app.weight.realplay.ScreenOrientationHelper;
import com.suchness.landmanage.data.been.AlarmMsg;
import com.suchness.landmanage.data.been.db.DBManager;
import com.suchness.landmanage.data.been.db.PicFilePath;
import com.suchness.landmanage.data.been.db.Verifcode;
import com.suchness.landmanage.data.been.db.VideoFilePath;
import com.suchness.landmanage.databinding.FragmentAlarmDetailBinding;
import com.suchness.landmanage.ui.adapter.ImageAdapter;
import com.suchness.landmanage.viewmodel.HomeViewModel;
import com.videogo.constant.Constant;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.InnerException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.remoteplayback.RemoteFileInfo;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.MediaScanner;
import com.videogo.util.RotateViewUtil;
import com.videogo.util.SDCardUtil;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.CustomRect;
import com.videogo.widget.CustomTouchListener;
import com.videogo.widget.TitleBar;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_IMAGE_PATH;
import static com.suchness.landmanage.app.AppConfig.DEFAULT_SAVE_VIDEO_PATH;
import static com.suchness.landmanage.app.ext.LoadingDialogExtKt.getOpenSDK;
import static com.videogo.util.Utils.showToast;

/**
 * @author: hejunfeng
 * @date: 2021/12/24 0024
 */
public class AlarmDetailFragment extends BaseFragment<HomeViewModel, FragmentAlarmDetailBinding> implements SurfaceHolder.Callback , View.OnTouchListener ,
        View.OnClickListener , VerifyCodeInput.VerifyCodeInputListener{
    // ??????????????????
    private InputMethodManager imm;
    private WaitDialog mWaitDlg = null;
    private AlarmMsg alarmMessage = null;
    private String mVerifyCode;
    private String ChanneNumber;
    private String CameraName = "";
    private String address = "";
    public String imgpath;
    public HashMap<String,String> map;
    private List<HashMap<String,String>> url_list = new ArrayList<>();
    // ??????????????????
    private RemoteFileInfo fileInfo;
    // ????????????????????????
    private boolean mShowNetworkTip = true;
    private List<EZCameraInfo> cameraInfoList = new ArrayList<>();
//    private Handler handler ;
    // ????????????
    private LocalInfo localInfo = null;
    private int mCaptureDisplaySec = 0;
    // ????????????
    private AudioPlayUtil mAudioPlayUtil = null;

    private RotateViewUtil mRecordRotateViewUtil = null;
    private RelativeLayout remoteListPage = null;
    private Rect mRemotePlayBackRect = null;
    // ??????????????????
    private int mControlDisplaySec = 0;
    // ??????
    private TitleBar mTitleBar;
    // ?????????
    private Timer mUpdateTimer = null;
    // ????????????????????????
    private TimerTask mUpdateTimerTask = null;
    // ???????????????
    private LoadingTextView loadingBar;
    // ?????????????????????
    private TextView remoteLoadingBufferTv, touchLoadingBufferTv;
    // ????????????
    private RelativeLayout remotePlayBackArea;
    // ??????????????????
    private TextView endTimeTV = null;
    // ????????????????????????
    private ImageButton exitBtn;
    // ?????????????????????
    private Button backBtn;
    // ????????????SurfaceView
    private SurfaceView surfaceView = null;
    private TextView mRemotePlayBackRatioTv = null;
    private CustomTouchListener mRemotePlayBackTouchListener = null;
    // ???????????????????????????????????????????????????
    private boolean notShowControlArea = true;
    // ????????????
    private float mPlayScale = 1;
    // ????????????
    private int status = RemoteListContant.STATUS_INIT;
    // ?????????????????????
    private AlertDialog mLimitFlowDialog = null;
    private int mCountDown = 10;
    private LinearLayout mRemotePlayBackRecordLy = null;
    // ???????????????
    private SeekBar progressSeekbar = null;
    private ProgressBar progressBar = null;
    private ImageView matteImage;
    // ??????????????????
    private TextView beginTimeTV = null;
    // ??????????????????
    private TextView errorInfoTV;
    // ??????????????????
    private ImageButton errorReplay;
    // ??????????????????
    private LinearLayout controlArea = null;
    private LinearLayout progressArea = null;
    // ??????
    private ImageButton captureBtn = null;
    // ??????
    private ImageButton videoRecordingBtn = null;
    // ????????????
    private ImageButton videoRecordingBtn_end = null;
    private View mRealPlayRecordContainer = null;
    // ????????????
    private LinearLayout downloadBtn = null;
    // ??????????????????
    private TextView fileSizeText;
    // Loading??????
    private LoadingView loadingImgView;
    private LinearLayout loadingPbLayout;
    private boolean  bIsRecording = false;
    private String mRecordTime = null;
    // ????????????
    private TextView flowTV = null;
    // ????????????
    private int mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    // loading??????????????????????????????
    private ImageButton loadingPlayBtn;
    // ??????/????????????
    private ImageButton pauseBtn = null;
    // ????????????
    private ImageButton soundBtn = null;
    private EZPlayer mPlayer = null;
    // ???????????????????????? ????????????
    private LinearLayout replayAndNextArea = null;
    // ???????????????
    private ImageView mRemotePlayBackRecordIv = null;
    // ????????????
    private int mRecordSecond = 0;
    // ????????????
    private TextView mRemotePlayBackRecordTv = null;
    // ????????????
    private ImageButton replayBtn;
    private long beginTime;
    private long endTime;
    private long sustainTime = 20*1000;
    private long forwardTime = 1*20*60*1000;
    // ?????????????????????
    private ImageButton nextPlayBtn;
    // ??????????????????????????????
    private LinearLayout touchProgressLayout;
    // ????????????
    private CheckTextButton mFullscreenButton;
    private ScreenOrientationHelper mScreenOrientationHelper;
    // ??????????????????????????????????????????
    private boolean notPause = true;
    // ???????????? */
    private int mRealFlow = 0;
    // ???????????????????????? */
    private long mStreamFlow = 0;
    private RelativeLayout mControlBarRL;
    private TitleBar mLandscapeTitleBar = null;
    private Context context;
    private static String TAG= "PlaybackActivity2";
    // ???????????????
    private float mRealRatio = Constant.LIVE_VIEW_RATIO;
    private SwipeRecyclerView swipeRecyclerView;
    private ImageAdapter imageAdapter;

    private Handler playBackHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                // ??????????????????
                // 380061???????????????>=???????????????????????????
                case ErrorCode.ERROR_CAS_RECORD_SEARCH_START_TIME_ERROR:
                    //Log.d(TAG, "ERROR_CAS_RECORD_SEARCH_START_TIME_ERROR");
                    handlePlaySegmentOver();
                    break;
                case EZConstants.EZPlaybackConstants.MSG_REMOTEPLAYBACK_PLAY_FINISH:
                    //Log.d(TAG, "MSG_REMOTEPLAYBACK_PLAY_FINISH");
                    loadingPlayBtn.setVisibility(View.VISIBLE);
                    handlePlaySegmentOver();
                    break;
                // ?????????????????????
                case EZConstants.EZPlaybackConstants.MSG_REMOTEPLAYBACK_PLAY_SUCCUSS:
                    handleFirstFrame(msg);
                    break;
                case EZConstants.EZPlaybackConstants.MSG_REMOTEPLAYBACK_STOP_SUCCESS:
                    handleStopPlayback();
                    break;
                case EZConstants.EZPlaybackConstants.MSG_REMOTEPLAYBACK_PLAY_FAIL:
                    ErrorInfo errorInfo = (ErrorInfo) msg.obj;
                    handlePlayFail(errorInfo);
                    break;
                // ????????????????????????
                case RemoteListContant.MSG_REMOTELIST_CONNECTION_EXCEPTION:
                    if (msg.arg1 == ErrorCode.ERROR_CAS_RECORD_SEARCH_START_TIME_ERROR) {
                        handlePlaySegmentOver();
                    } else {
                        String detail = (msg.obj != null ? msg.obj.toString() : "");
                        //handleConnectionException(msg.arg1, detail);
                    }
                    break;
                case RemoteListContant.MSG_REMOTELIST_UI_UPDATE:
                    updateRemotePlayUI();
                    break;
                case RemoteListContant.MSG_REMOTELIST_STREAM_TIMEOUT:
                    //handleStreamTimeOut();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public int layoutId() {
        return R.layout.fragment_alarm_detail;
    }

    @Override
    public void initView(@org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        // ??????????????????
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWaitDlg = new WaitDialog(getContext(), android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDlg.setCancelable(false);
        getData();

        //??????????????????
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.delate.pic");
        try {
            initUi();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        onQueryExceptionLayoutTouched();
        initRemoteListPlayer();
        fakePerformClickUI();
        initEzPlayer();
        initListener();
//        handler = new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                switch (msg.what){
//                    case 256:
//                        CameraName = getCameraInfo(cameraInfoList,ChanneNumber);
//                        mLandscapeTitleBar.setTitle(CameraName);
//                        break;
//                }
//            }
//        };
    }


    private void initUi() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        mRecordRotateViewUtil = new RotateViewUtil();

        remoteListPage = (RelativeLayout)mDatabind.getRoot().findViewById(R.id.remote_list_page);
        mTitleBar = (TitleBar) mDatabind.getRoot().findViewById(R.id.title);
        /** ????????????????????? **/
        ViewTreeObserver viewTreeObserver = remoteListPage.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mRemotePlayBackRect == null) {
                    // ?????????????????????
                    mRemotePlayBackRect = new Rect();
                    getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(mRemotePlayBackRect);
                }
            }
        });

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
        fileSizeText = (TextView) mDatabind.getRoot().findViewById(R.id.file_size_text);
        measure(downloadBtn);
        measure(controlArea);
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


        touchProgressLayout = (LinearLayout) mDatabind.getRoot().findViewById(R.id.touch_progress_layout);

        mFullscreenButton = (CheckTextButton) mDatabind.getRoot().findViewById(R.id.fullscreen_button);
        mScreenOrientationHelper = new ScreenOrientationHelper(getActivity(), mFullscreenButton);
        notPause = true;
        mControlBarRL = (RelativeLayout) mDatabind.getRoot().findViewById(R.id.flow_area);

        mLandscapeTitleBar = (TitleBar) mDatabind.getRoot().findViewById(R.id.pb_title_bar_landscape);
        mLandscapeTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff), getResources().getDrawable(R.color.dark_bg_70p),
                getResources().getDrawable(R.drawable.message_back_selector_1));
        mLandscapeTitleBar.setOnTouchListener(this);
        mLandscapeTitleBar.addBackButton(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private void initEzPlayer() {
        newPlayInit(true, true);
        showControlArea(true);
        timeBucketUIInit(beginTime, endTime);
        if (alarmMessage.getChannel()!=null && !alarmMessage.getChannel().equals("")){
            mPlayer.setHandler(playBackHandler);
            mPlayer.setSurfaceHold(surfaceView.getHolder());
            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.setTime(new Date(beginTime));
            end.setTime(new Date(endTime));
            mPlayer.startPlayback(begin,end);
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
    private void onQueryExceptionLayoutTouched() {
        mTitleBar.setTitle("????????????");

    }
    private void initListener() {
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff),getResources().getDrawable(R.color.colorPrimary),
                null);
//        backBtn = mTitleBar.addBackButton(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onExitCurrentPage();
//                finish();
//            }
//        });

        exitBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onPlayExitBtnOnClick();
            }
        });
        // loading??????????????????
        loadingPlayBtn.setOnClickListener(this);
        // ??????????????????
        replayBtn.setOnClickListener(this);
        errorReplay.setOnClickListener(this);
        // ??????????????????????????????
        nextPlayBtn.setOnClickListener(this);
        // ????????????touch??????
        remotePlayBackArea.setOnTouchListener(this);
        // ????????????touch??????
        controlArea.setOnTouchListener(this);
        controlArea.setOnClickListener(this);
        // ????????????????????????
        pauseBtn.setOnClickListener(this);
        // ??????????????????
        soundBtn.setOnClickListener(this);
        // ????????????????????????
        exitBtn.setOnClickListener(this);
        // ??????????????????
        captureBtn.setOnClickListener(this);
        // ??????????????????
        videoRecordingBtn.setOnClickListener(this);

        videoRecordingBtn_end.setOnClickListener(this);
        // ??????/????????????????????????????????????

        progressSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * ????????????????????????????????????
             */
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                int progress = arg0.getProgress();
                if (progress == RemoteListContant.PROGRESS_MAX_VALUE) {
                    stopRemoteListPlayer();
                    handlePlaySegmentOver();
                    return;
                }
                if (CameraName != null) {
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
                        Calendar stopTime = Calendar.getInstance();
                        stopTime.setTime(new Date(endTime));
                        mPlayer.stopPlayback();
                        mPlayer.startPlayback(seekTime,stopTime);
                    }
                }
            }

            /**
             * ????????????????????????????????????
             */
            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            /**
             * ????????????????????????????????????
             */
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                if (CameraName != null) {
                    long time = endTime-beginTime;
                    int diffSeconds = (int) (time * arg1 / 1000) / 1000;
                    String convToUIDuration = RemoteListUtil.convToUIDuration(diffSeconds);
                    beginTimeTV.setText(convToUIDuration);
                }
            }
        });
    }
    // ??????????????????????????????
    private void onPlayExitBtnOnClick() {
        stopRemoteListPlayer();
        //remotePlayBackArea.setVisibility(View.GONE);
        // ?????????????????????
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
    }
    private void stopRemoteListPlayer() {
        try {
            if(mPlayer != null) {
                mPlayer.stopPlayback();
                LogUtil.i(TAG, "stop");
                mPlayer.stopLocalRecord();
            }
            mRealFlow = localInfo.getLimitFlow();
            mStreamFlow = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initRemoteListPlayer() {
        stopRemoteListPlayer();
        if (status != RemoteListContant.STATUS_DECRYPT) {
            status = RemoteListContant.STATUS_INIT;
        }
    }
    private void fakePerformClickUI() {
        fileSizeText.setText("");
        downloadBtn.setPadding(0, 0, 0, 0);
        remotePlayBackArea.setVisibility(View.VISIBLE);
        errorReplay.setVisibility(View.GONE);
        loadingPlayBtn.setVisibility(View.GONE);
        hideControlArea();
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

        // ??????????????????????????????
        if (notPause || status == RemoteListContant.STATUS_DECRYPT) {
            surfaceView.setVisibility(View.VISIBLE);
            startUpdateTimer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closePlayBack();
        if (mPlayer != null) {
            getOpenSDK().releasePlayer(mPlayer);
        }
        removeHandler(playBackHandler);
//        removeHandler(handler);
        if (mUpdateTimer != null){
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
        if (mUpdateTimerTask != null){
            mUpdateTimerTask.cancel();
            mUpdateTimerTask = null;
        }
    }

    public void removeHandler(Handler handler) {
//        this.handler = handler;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        mScreenOrientationHelper.postOnStart();
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

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }
    
    
//    @Override
//    public void onRestart() {
//        super.onRestart();
//        initEzPlayer();
//        Log.d(TAG,"onrestart");
//    }

    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            mScreenOrientationHelper.portrait();
            return;
        }
        if (backBtn != null && backBtn.getVisibility() == View.GONE) {
        } else {
            onExitCurrentPage();
//            finish();
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
                if (controlArea.getVisibility() == View.VISIBLE && mControlDisplaySec < 5
                        && status != RemoteListContant.STATUS_INIT) {
                    mControlDisplaySec++;
                }
                // ????????????
                if (mLimitFlowDialog != null && mLimitFlowDialog.isShowing() && mCountDown > 0) {
                    mCountDown--;
                }
                // ????????????
                if (bIsRecording) {
                    // ??????????????????
                    Calendar OSDTime = null;
                    if(mPlayer != null)
                        OSDTime = mPlayer.getOSDTime();
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
        // ??????1000ms????????????1000ms????????????
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
                // ????????????????????????????????????
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
    private void handlePlayProgress(Calendar osdTime) {
        long osd = osdTime.getTimeInMillis();
        double x = ((osd - beginTime) * RemoteListContant.PROGRESS_MAX_VALUE) / (double) (endTime - beginTime);
        int progress = (int) x;
        progressSeekbar.setProgress(progress);
        progressBar.setProgress(progress);
        int beginTimeClock = (int) ((osd - beginTime) / 1000);
        updateTimeBucketBeginTime(beginTimeClock);
    }
    private void updateTimeBucketBeginTime(int beginTimeClock) {
        String convToUIDuration = RemoteListUtil.convToUIDuration(beginTimeClock);
        beginTimeTV.setText(convToUIDuration);
    }
    private void dismissPopDialog(AlertDialog popDialog) {
        if (popDialog != null && popDialog.isShowing() ) {
            try {
                popDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // ????????????
    private void stopRemotePlayBackRecord() {
        if (!bIsRecording) {
            return;
        }
        mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
        showToast(getContext(),getResources().getString(R.string.already_saved_to_volume));
        if(mPlayer != null) {
            mPlayer.stopLocalRecord();
        }
        // ?????????????????????
        mRemotePlayBackRecordLy.setVisibility(View.GONE);
        mRecordRotateViewUtil.applyRotation(mRealPlayRecordContainer, videoRecordingBtn_end, videoRecordingBtn, 0, 90);
        mCaptureDisplaySec = 0;
    }
    // ??????????????????
    private void updateRecordTime() {
        if (mRemotePlayBackRecordIv.getVisibility() == View.VISIBLE) {
            mRemotePlayBackRecordIv.setVisibility(View.INVISIBLE);
        } else {
            mRemotePlayBackRecordIv.setVisibility(View.VISIBLE);
        }
        // ????????????
        int leftSecond = mRecordSecond % 3600;
        int minitue = leftSecond / 60;
        int second = leftSecond % 60;

        // ??????????????????
        String recordTime = String.format("%02d:%02d", minitue, second);
        mRemotePlayBackRecordTv.setText(recordTime);
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
        flowTV.setText("0k/s 0MB");
        downloadBtn.setPadding(Utils.dip2px(getContext(), 5), 0, Utils.dip2px(getContext(), 5), 0);
        if (localInfo.isSoundOpen()) {
            // ????????????
            if(mPlayer != null) {
                mPlayer.openSound();
            }
        } else {
            // ????????????
            if(mPlayer != null) {
                mPlayer.closeSound();
            }
        }
    }
    // ??????????????????????????????????????????
    private void handleStopPlayback() {
        LogUtil.d(TAG, "stop playback success");
    }
    // ??????????????????
    private void handlePlayFail(ErrorInfo errorInfo) {

        LogUtil.d(TAG, "handlePlayFail. Playback failed. error info is " + errorInfo.toString());
        status = RemoteListContant.STATUS_STOP;
        stopRemoteListPlayer();

        int errorCode = errorInfo.errorCode;

        switch (errorCode) {
            case ErrorCode.ERROR_TRANSF_ACCESSTOKEN_ERROR:{
            }
            // ????????????????????????????????????????????????????????????????????????????????????????????????
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
                    // ??????????????????
                    txt = getString(R.string.camera_not_online);
                } else {
                    if (errorCode!=0){
                        //txt = getErrorTip(R.string.remoteplayback_fail, errorCode);
                        txt = errorInfo.description;
                        if (txt.equals("???????????????????????????")){
                            txt = "???????????????????????????";
                        }
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
        errorInfoTV.setText(txt);
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
    // ?????????????????? UI????????????
    private void resetPauseBtnUI() {
        notPause = true;
        pauseBtn.setBackgroundResource(R.drawable.ez_remote_list_pause_btn_selector);
    }
    private void newSeekPlayUIInit() {
        touchProgressLayout.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        exitBtn.setVisibility(View.GONE);
        replayAndNextArea.setVisibility(View.GONE);
        errorInfoTV.setVisibility(View.GONE);
        errorReplay.setVisibility(View.GONE);
        // ?????????????????????
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
    /**
     * <p>
     * ???????????????
     * </p>
     *
     * @author hanlieng 2014-8-4 ??????9:04:24
     */
    private void onExitCurrentPage() {
        notPause = true;
        closePlayBack();
    }
    private void closePlayBack() {
        if (status == RemoteListContant.STATUS_EXIT_PAGE) {
            return;
        }
        LogUtil.d(TAG, "????????????.........");
        stopRemoteListPlayer();

        onActivityStopUI();
        stopUpdateTimer();
        status = RemoteListContant.STATUS_EXIT_PAGE;
    }
    // ??????????????????UI
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
    // ???????????????
    private void stopUpdateTimer() {
        mControlDisplaySec = 0;
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
    private void measure(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
    }
    private void setRemoteListSvLayout() {
        // ????????????????????????
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
    private EZCameraInfo getmCameraInfo(List<EZCameraInfo> cameraInfos,String channeNumber){
        if (channeNumber!=null&&!channeNumber.equals("")){
            for (EZCameraInfo cameraInfo : cameraInfos){
                if (cameraInfo.getCameraNo() == Integer.parseInt(channeNumber)){
                    return cameraInfo;
                }
            }
        }
        return cameraInfos.get(1);
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
    private void getData() {
        context = getContext();
        localInfo = LocalInfo.getInstance();
        Bundle bundle = getArguments();
        if (bundle != null) {
            alarmMessage = bundle.getParcelable("msg");
            ChanneNumber = alarmMessage.getChannel();
            String time = alarmMessage.getStartTime();
            CameraName = alarmMessage.getDevDesc();
            if (!time.equals("")){
                try {
                    if (time.contains("-")){
                        beginTime = Long.parseLong(DataUtils.date2TimeStamp(time,"yyyy-MM-dd HH:mm:ss"))-2000;
                        Log.d(TAG,"beginTime="+beginTime);
                        endTime = beginTime+sustainTime;
                    }else{
                        beginTime = Long.parseLong(time)-2000;
                        endTime = beginTime+sustainTime;
                    }
                    Log.d(TAG,"beginTime2="+beginTime);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            List<String> urls = new ArrayList<>();
            if (!TextUtils.isEmpty(alarmMessage.getImgPath())){
                urls = Arrays.asList(alarmMessage.getImgPath().split(","));
            }
            swipeRecyclerView = mDatabind.getRoot().findViewById(R.id.recyclerView);
            imageAdapter = new ImageAdapter(R.layout.item_img,urls);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            swipeRecyclerView.setLayoutManager(linearLayoutManager);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            View headerView = LayoutInflater.from(getContext()).inflate(R.layout.item_header,null);
            TextView t1 = headerView.findViewById(R.id.message);
            t1.setText(alarmMessage.getAlarmType());
            TextView t2 = headerView.findViewById(R.id.area);
            t2.setText(alarmMessage.getArea());

            TextView t3 = headerView.findViewById(R.id.devdesc);
            t3.setText(alarmMessage.getDevDesc());
            TextView t4 = headerView.findViewById(R.id.time);
            t4.setText(alarmMessage.getStartTime());
            LinearLayout speedlayout = headerView.findViewById(R.id.speedlayout);
            if (alarmMessage.getAverageSpeed() != null && alarmMessage.getAlarmType().equals("????????????")){
                speedlayout.setVisibility(View.VISIBLE);
                TextView t5 = headerView.findViewById(R.id.average_speed);
                t5.setText(alarmMessage.getAverageSpeed()+" km/h");
            }else{
                speedlayout.setVisibility(View.GONE);
            }
            swipeRecyclerView.addHeaderView(headerView);
            swipeRecyclerView.setAdapter(imageAdapter);
            List<String> finalUrls = urls;
            imageAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull @NotNull BaseQuickAdapter<?, ?> baseQuickAdapter, @NonNull @NotNull View view, int i) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("position",i);
                    bundle.putSerializable("urls", (Serializable) finalUrls);
                    NavHostFragment.findNavController(AlarmDetailFragment.this).navigate(R.id.action_to_preview_pic_fragment,bundle);
                }
            });
        }
        mAudioPlayUtil = AudioPlayUtil.getInstance(App.Companion.getInstance());
        DisplayMetrics metric = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
        localInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        localInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(mPlayer != null) {
            mPlayer.setSurfaceHold(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mPlayer != null) {
            mPlayer.setSurfaceHold(null);
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
            default:
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // TODO
            case R.id.query_exception_ly:
                onQueryExceptionLayoutTouched();
                break;
            case R.id.loading_play_btn:
                loadingPlayBtn.setVisibility(View.GONE);
                onReplayBtnClick();
                break;
            case R.id.error_replay_btn:
            case R.id.replay_btn:
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
                break;
            case R.id.control_area:
                break;
            default:
                break;
        }
    }
    // ????????????????????????
    private void onReplayBtnClick() {
        newPlayInit(true, true);
        showControlArea(true);
        timeBucketUIInit(beginTime, endTime);
        Calendar begin = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        begin.setTime(new Date(beginTime));
        end.setTime(new Date(endTime));
        mPlayer.startPlayback(begin,end);
    }
    private void timeBucketUIInit(long beginTime, long endTime) {
        int diffSeconds = (int) (endTime - beginTime) / 1000;
        String convToUIDuration = RemoteListUtil.convToUIDuration(diffSeconds);
        beginTimeTV.setText(RemoteListContant.VIDEO_DUAR_BEGIN_INIT);
        endTimeTV.setText(convToUIDuration);
    }
    // ????????????????????????
    private void onPlayPauseBtnClick() {
        if (notPause) {
            // ????????????
            notPause = false;
            pauseBtn.setBackgroundResource(R.drawable.remote_list_play_btn_selector);
            if (status != RemoteListContant.STATUS_PLAYING) {
                pauseStop();
            } else {
                status = RemoteListContant.STATUS_PAUSE;
                if(mPlayer != null) {
                    // ????????????
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
                // ????????????
                if(mPlayer != null) {
                    mPlayer.resumePlayback();
                }
                mScreenOrientationHelper.disableSensorOrientation();
                status = RemoteListContant.STATUS_PLAYING;
            }
        }
    }
    // ????????????
    private void onSoundBtnClick() {
        if(mPlayer == null) {
            return;
        }
        if (localInfo.isSoundOpen()) {
            // ????????????
            localInfo.setSoundOpen(false);
            mPlayer.closeSound();
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundoff_btn_selector);
        } else {
            // ????????????
            localInfo.setSoundOpen(true);
            mPlayer.openSound();
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundon_btn_selector);
        }
    }
    // ????????????
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
            // ??????SD????????????
            showToast(getContext(),R.string.remoteplayback_SDCard_disable_use);
            return;
        }

        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
            mRemotePlayBackRecordLy.setVisibility(View.GONE);
            mRemotePlayBackRecordTv.setText("00:00");
            mRecordSecond = 0;
            // ??????????????????
            showToast(getContext(),R.string.remoteplayback_record_fail_for_memory);
            return;
        }
        mRemotePlayBackRecordLy.setVisibility(View.VISIBLE);
        mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
        if(mPlayer != null) {
            // ????????????deviceSerial+???????????????????????????demo??????????????????????????????
            Date date = new Date();
            String strRecordFile = DEFAULT_SAVE_VIDEO_PATH + CameraName+"/"
                    + String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) + ".mp4";
            mPlayer.startLocalRecordWithFile(strRecordFile);

            //????????????
            List<VideoFilePath> files = DBManager.getInstance().get(VideoFilePath.class);
            if (files.size()>=200){
                DBManager.getInstance().delete(VideoFilePath.class,"path=",new String[]{files.get(0).getPath()});
            }
            String name = String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) +".jpg";
            DBManager.getInstance().insert(new VideoFilePath(strRecordFile, name));
            mRecordRotateViewUtil.applyRotation(mRealPlayRecordContainer, videoRecordingBtn,
                    videoRecordingBtn_end, 0, 90);
        }
    }
    // ????????????????????????
    private void onCapturePicBtnClick() {
        java.util.Date date = new java.util.Date();
        String path = DEFAULT_SAVE_IMAGE_PATH +CameraName+"/"
                + String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) +".jpg";
        mControlDisplaySec = 0;
        if (!SDCardUtil.isSDCardUseable()) {
            // ??????SD????????????
            showToast(getContext(),R.string.remoteplayback_SDCard_disable_use);
            return;
        }
        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
            // ??????????????????
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
                        //????????????
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
    // ????????????????????????
    private void pauseStop() {
        status = RemoteListContant.STATUS_STOP;
        stopRemoteListPlayer();
        loadingImgView.setVisibility(View.GONE);
        loadingPbLayout.setVisibility(View.GONE);
        loadingPlayBtn.setVisibility(View.VISIBLE);
    }
    private void pausePlay() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // ?????????????????????
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
        if (CameraName != null) {
            reConnectPlay(startTime);
        }

    }
    private Calendar getTimeBarSeekTime() {
        if (CameraName != null) {
            int progress = progressSeekbar.getProgress();
            long seekTime = (((endTime - beginTime) * progress) / RemoteListContant.PROGRESS_MAX_VALUE) + beginTime;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(seekTime);
            return c;
        }
        return null;
    }
    // ??????
    private void reConnectPlay(Calendar uiPlayTimeOnStop) {
        newPlayInit(false, false);
        RemoteFileInfo fileInfo1 = this.fileInfo.copy();
        fileInfo1.setStartTime(uiPlayTimeOnStop);
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

    private void initEZPlayer() {
        if(mPlayer != null) {
            // ????????????
            mPlayer.stopLocalRecord();
            // ????????????
            mPlayer.stopPlayback();
        } else {
            if (alarmMessage.getChannel()!=null && !alarmMessage.getChannel().equals("")) {
                mPlayer = getOpenSDK().createPlayer(alarmMessage.getSerialNumber(), Integer.parseInt(alarmMessage.getChannel()));
                String name = alarmMessage.getSerialNumber() + String.valueOf(ChanneNumber);
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
    }
    // ????????????UI?????????
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
        // ?????????????????????
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = newConfig.orientation;
        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }

    private void onOrientationChanged() {
        setRemoteListSvLayout();
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // ???????????????
            fullScreen(false);
            if (status != RemoteListContant.STATUS_PLAYING) {
                // ?????????????????????
                mScreenOrientationHelper.disableSensorOrientation();
            }
            // ????????????
            remoteListPage.setBackgroundColor(getResources().getColor(R.color.white));
            mTitleBar.setVisibility(View.VISIBLE);
            if (controlArea.getVisibility() == View.VISIBLE) {
                captureBtn.setVisibility(View.GONE);
                videoRecordingBtn.setVisibility(View.VISIBLE);
            }
            mControlBarRL.setVisibility(View.VISIBLE);
            mLandscapeTitleBar.setVisibility(View.GONE);
        } else {
            // ????????????
            // ???????????????
            fullScreen(true);
            remoteListPage.setBackgroundColor(getResources().getColor(R.color.black_bg));
            mTitleBar.setVisibility(View.GONE);
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
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getActivity().getWindow().setAttributes(attr);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }


    @Override
    public void onInputVerifyCode(String verifyCode) {
        if (mPlayer != null) {
            String name = alarmMessage.getSerialNumber()+String.valueOf(ChanneNumber);
            if (mVerifyCode == null){
                DBManager.getInstance().insert(new Verifcode(name, verifyCode));
            }else{
                DBManager.getInstance().update(new Verifcode(name, verifyCode),"name=?", new String[]{name});
            }

            newPlayUIInit();
            showControlArea(true);
            mVerifyCode = verifyCode;
            if (mPlayer != null){
                LogUtil.d(TAG, "verify code is " + verifyCode);
                mPlayer.setPlayVerifyCode(mVerifyCode);
            }
            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.setTime(new Date(beginTime));
            end.setTime(new Date(endTime));
            mPlayer.startPlayback(begin,end);
            Log.d("TAG","******mPlayer.startPlayback********");
        }
    }


    private String getCameraInfo(List<EZCameraInfo> cameraInfos , String no){
        if (no!=null&&!no.equals("")){
            for (EZCameraInfo cameraInfo : cameraInfos){
                if (cameraInfo.getCameraNo() == Integer.parseInt(no)){
                    Log.d("TAG","no="+cameraInfo.getDeviceSerial());
                    return cameraInfo.getCameraName();
                }
            }
        }
        return "Null";
    }


//    public void queryLocation(TextView textView , String la, String ln) throws UnsupportedEncodingException, NoSuchAlgorithmException {
//        String url = AlarmContant.location_url;
//        LinkedHashMap<String,String> map = new LinkedHashMap<>();
//        map.put("location",la+","+ln);
//        map.put("coordtype","wgs84ll");
//        map.put("radius","500");
//        map.put("extensions_poi","1");
//        map.put("output","json");
//        map.put("ak","KNAeq1kjoe2u24PTYfeL4kO0KvGaqNak");
//        String sn = SnCal.getSnKry(map);
//        OkHttpUtil.get(url, sn,new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.d("TAG", "onFailure: ",e);
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String responseBody = response.body().string();
//                String address = "";
//                try {
//                    JSONObject object = new JSONObject(responseBody);
//                    String status = object.get("status").toString();
//                    if (status.equals("0")){
//                        String result = object.get("result").toString();
//                        JSONObject objectdata = new JSONObject(result);
//                        String formatted_address = objectdata.get("formatted_address").toString();
//                        String sematic_description = objectdata.get("sematic_description").toString();
//                        if (sematic_description==null || sematic_description.equals("")){
//                            address = formatted_address;
//                        }else{
//                            address = formatted_address+"("+sematic_description+")";
//                        }
//                        if (address.equals("")){
//                            Message message = Message.obtain();
//                            message.what = 301;
//                            playBackHandler.sendMessage(message);
//                        }else{
//                            Message message = Message.obtain();
//                            message.what = 302;
//                            Bundle bundle = new Bundle();
//                            bundle.putString("address",address);
//                            message.setData(bundle);
//                            playBackHandler.sendMessage(message);
//                        }
//                    }else{
//                        Message message = Message.obtain();
//                        message.what = 301;
//                        playBackHandler.sendMessage(message);
//                    }
//                    Log.d("TAG","address="+address);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        },map);
//    }

//    public class MyReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Picasso.with(context).invalidate(new File(imgpath));
//            imageView.setImageDrawable(null);
//            //donut_progress.setVisibility(View.VISIBLE);
//            //????????????
//            String pic_name = map.get("pic_name");
//            String[] pic = pic_name.split("\\.");
//            String name = pic[0]+short_str+"."+pic[1];
//            imgpath = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/cash/"+name;
//            File imgFile = new File(imgpath);
//            asyncImageLoaderPic.loadDrawable(map,donut_progress , short_str,new AsyncImageLoaderPic.ImageCallback() {
//                @Override
//                public void imageLoaded() {
//                    Picasso.with(context).load(imgFile).transform(new RoundTransform(20))
//                            .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(imageView);
//                }
//
//                @Override
//                public void imageLoadEmpty() {
//                    File imgFile = new File(Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/cash/"+pic_name);
//                    Log.d(TAG,"img="+imgFile.toString());
//                    Picasso.with(context).load(imgFile).transform(new RoundTransform(20))
//                            .error(context.getResources().getDrawable(R.mipmap.load_fail2)).into(imageView);
//                }
//
//                @Override
//                public void imageLoadLocal() {
//
//                }
//            });
//        }
//    }
}
