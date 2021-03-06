package com.suchness.landmanage.data.been.db;

import android.content.Context;
import android.os.Environment;

import com.suchness.landmanage.app.App;
import com.suchness.landmanage.app.utils.StreamUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @description 数据库
 * @author hejunfeng
 * @time 2020/7/17 0017
 */
@SuppressWarnings("unused")
public class DBManager extends DBHelper {
    private DBManager(Context context) {
        super(context);
    }

    private static DBManager mInstance;
    private static DBManager mCountryManager;

    public static void init(Context context) {
        if (mInstance == null) {
            mInstance = new DBManager(context);
        }
        if (mCountryManager == null) {
            mCountryManager = getAssetSQLite(context);
        }
    }

    private DBManager(Context context, String name) {
        super(context, name);
    }

    public static DBManager getInstance() {
        return mInstance;
    }

    public static DBManager getCountryManager() {
        if (mCountryManager == null) {
            mCountryManager = getAssetSQLite(App.Companion.getInstance());
        }
        return mCountryManager;
    }

    /**
     * 打开assets的数据库
     *
     * @param context context
     * @return SQLiteDatabase
     */
    private static DBManager getAssetSQLite(Context context) {
        try {
            String path = Environment.getDataDirectory().getAbsolutePath() + "/data/" + context.getPackageName() + "/databases/osc_data.db";
            if (!new File(path).exists()) {
                InputStream is = context.getAssets().open("data.db");
                inputStreamToFile(is, path);
            }
            DBManager manager = new DBManager(context, "osc_data.db");
            if (!manager.isExist("city")) {
                context.deleteDatabase("osc_data.db");
                InputStream is = context.getAssets().open("data.db");
                inputStreamToFile(is, path);
            }
            return new DBManager(context, "osc_data.db");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static void inputStreamToFile(InputStream is, String newPath) {
        FileOutputStream fs = null;
        try {
            int read;
            fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1444];
            while ((read = is.read(buffer)) != -1) {
                fs.write(buffer, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.close(fs, is);
        }
    }
}
