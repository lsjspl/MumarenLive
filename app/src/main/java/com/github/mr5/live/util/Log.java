package com.github.mr5.live.util;



/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class Log {
    private final static String TAG = "Tvbox5555";

    public static void e(String msg,Throwable e) {
        android.util.Log.e(TAG, msg,e);
    }

    public static void e(String msg) {
        android.util.Log.e(TAG, msg);
    }


    public static void i(String msg) {
        android.util.Log.i(TAG, msg);
    }

    public static void d(String msg) {
        android.util.Log.i(TAG, msg);
    }

}