package com.cilys.utils.utils.job;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.litesuits.orm.LiteOrm;

import java.io.File;

/**
 * @author cily
 * @version 1.0.0   2017-01-23  日志写数据库工具类
 */
public class DbUtils {
    private static LiteOrm liteOrm;
    private static boolean saveLog = false;

    public static void init(Context cx) {
        init(cx, false);
    }

    public static void init(Context cx, boolean saveExternal) {
        DbUtils.saveLog = saveLog;

        if (cx == null) {
            return;
        }

//         日志保存到db还是file
        if (saveExternal) {
            PackageManager pm = cx.getPackageManager();
            boolean readPermission = (PackageManager.PERMISSION_GRANTED ==
                    pm.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, cx.getPackageName()));

            boolean writePermission = (PackageManager.PERMISSION_GRANTED ==
                    pm.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, cx.getPackageName()));

            if (!readPermission || !writePermission) {
                return;
            }

            if (liteOrm == null) {
                liteOrm = LiteOrm.newSingleInstance(cx, Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                                + cx.getPackageName() + File.separator + "db_log.db");
            }
        } else {
            if (liteOrm == null) {
                liteOrm = LiteOrm.newSingleInstance(cx, "db_log.db");
            }
        }
    }

    public static LiteOrm getLiteOrm() {
        return liteOrm;
    }

    public static void setSaveLog(boolean saveLog) {
        DbUtils.saveLog = saveLog;
    }

    public static boolean isSaveLog() {
        return saveLog;
    }

    private static int num = 0;


}
