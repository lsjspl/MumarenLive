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
            String fileName = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".txt";

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


    public static File createFile(String directoryName, String fileName) {
        File directory;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 使用 MediaStore API 在外部存储的 Downloads 目录下创建文件
            directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        } else {
            // 在 Android 10 以下版本可以继续使用旧的方式
            directory = new File(Environment.getExternalStorageDirectory(), directoryName);
        }

        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    // 在 Android 10 上使用 MediaStore API 写入文件
    public static Uri writeToFile(Context context, String directoryName, String fileName, String content) {
        File file = createFile(directoryName, fileName);
        Uri uri = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + directoryName);

                uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

                if (uri != null) {
                    try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                        if (outputStream != null) {
                            outputStream.write(content.getBytes());
                        }
                    }
                }
            } else {
                // 在 Android 10 以下版本可以继续使用旧的方式
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    fileOutputStream.write(content.getBytes());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return uri;
    }

}