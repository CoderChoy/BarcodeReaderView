package com.coderchoy.barcodereaderview.util;

import android.util.Log;

/**
 * Debug信息开关
 * <p>
 * Created by Leo
 * on 2017/3/21.
 */

public class LogEx {

    public static final boolean DEBUG = false;

    private LogEx() {
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        w(tag, msg, null);
    }

    public static void w(String tag, Throwable tr) {
        w(tag, "", tr);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.w(tag, msg, tr);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void wtf(String tag, String msg) {
        if (DEBUG) {
            Log.wtf(tag, msg);
        }
    }

}
