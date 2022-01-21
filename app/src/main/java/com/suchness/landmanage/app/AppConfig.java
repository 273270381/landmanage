package com.suchness.landmanage.app;

import android.content.Context;
import android.os.Environment;
import java.io.File;
/**
 * 应用程序配置类
 * 用于保存用户相关信息及设置
 */
public class AppConfig {
    private final static String APP_CONFIG = "config";

    // 默认存放影像文件的路径
    public final static String DEFAULT_SAVE_TIF_PATH = App.Companion.getInstance().getExternalFilesDir(Environment.DIRECTORY_DCIM )
            + File.separator
            + "landmanage"
            + File.separator + "Map" + File.separator;


    // 默认存放图片的路径
    public final static String DEFAULT_SAVE_IMAGE_PATH = App.Companion.getInstance().getExternalFilesDir(Environment.DIRECTORY_DCIM )
            + File.separator
            + "landmanage"
            + File.separator + "CapturePicture" + File.separator;

    // 默认存放视频的路径
    public final static String DEFAULT_SAVE_VIDEO_PATH = App.Companion.getInstance().getExternalFilesDir(Environment.DIRECTORY_DCIM )
            + File.separator
            + "landmanage"
            + File.separator + "CaptureVideo" + File.separator;

    private Context mContext;
    private static AppConfig appConfig;

    public static AppConfig getAppConfig(Context context) {
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.mContext = context;
        }
        return appConfig;
    }
}
