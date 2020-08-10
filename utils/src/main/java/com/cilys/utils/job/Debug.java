package com.cilys.utils.job;

import java.text.SimpleDateFormat;

public class Debug {
    private static boolean DEBUG = false;

    protected static void setDebug(boolean DEBUG) {
        Debug.DEBUG = DEBUG;
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss SSS");
    public static void println(String level, String tag, String msg, Throwable e) {
        if (DEBUG) {
            System.out.println("[" + sdf.format(System.currentTimeMillis()) + "] [" + level + "] " + (tag == null ? "" : tag) + " " + msg);
        }
    }

}
