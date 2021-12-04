package com.cilys.utils.job.core;

import java.text.SimpleDateFormat;

public class Debug {
    private static int logLevel = Level.NONE;

    static void setLogLevel(int logLevel) {
        Debug.logLevel = logLevel;
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
    static void println(int level, String tag, String msg, Throwable e) {
        if (logLevel <= level) {
            System.out.println("[" + sdf.format(System.currentTimeMillis()) + "] [" + getLevelName(level) + "] " + (tag == null ? "" : tag) + " " + msg);
        }
    }

    private static String getLevelName(int level){
        if (level == Level.VERBOSE) {
            return "VERBOSE";
        } else if (level == Level.DEBUG) {
            return "DEBUG";
        } else if (level == Level.INFO) {
            return "INFO";
        } else if (level == Level.WARN) {
            return "WARN";
        } else if (level == Level.ERROR) {
            return "ERROR";
        }
        return "";
    }

    static void debug(String msg){
        debug(msg, null);
    }

    static void debug(String msg, Throwable e) {
        println(Level.DEBUG, null, msg, e);
    }

    static void verbose(String msg){
        verbose(msg, null);
    }

    static void verbose(String msg, Throwable e) {
        println(Level.VERBOSE, null, msg, e);
    }

    static void error(String msg, Throwable e) {
        println(Level.ERROR, null, msg, e);
    }

    static void warn(String msg, Throwable e) {
        println(Level.WARN, null, msg, e);
    }

    public interface Level {
        int VERBOSE = 0;
        int DEBUG = 1;
        int INFO = 2;
        int WARN = 3;
        int ERROR = 4;
        int NONE = 5;
    }
}