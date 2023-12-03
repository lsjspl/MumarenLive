package com.github.mr5.live.util;


import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class Log {
    private final static String TAG = "mumaren5555";

    public static void e(String msg, Throwable e) {
        android.util.Log.e(TAG, msg, e);
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

    private static String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public  static  void deleteLogFile(){
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File logsDir = new File(externalStorageDir, "Download");
        File logFile = new File(logsDir, "mumarenlog.txt");
        if(logFile.exists()){
            logFile.delete();
        }
    }

    // 将日志写入文件
    public static void writeLogToFile(String logMessage) {
        try {
            // 获取外部存储目录，通常是 /sdcard/
            File externalStorageDir = Environment.getExternalStorageDirectory();

            // 创建一个名为 "MyAppLogs" 的目录，用于存储日志文件
            File logsDir = new File(externalStorageDir, "Download");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            // 生成一个唯一的文件名，使用时间戳
            String fileName =  "mumarenlog.txt";

            // 创建日志文件
            File logFile = new File(logsDir, fileName);
            if(!logFile.exists()){
                logFile.createNewFile();
            }

            // 使用 FileWriter 将日志写入文件
            FileWriter fileWriter = new FileWriter(logFile, true);
            fileWriter.write(logMessage);
            fileWriter.write("\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,  e);
        }
    }


}