package com.mad.pogoenhancer;

import android.util.Log;

import timber.log.Timber;


public class Logger {
    public static void fatal(String TAG, String message) {
        Timber.wtf(message);
        Log.wtf(TAG, message);
    }

    public static void error(String TAG, String message) {
        Log.e(TAG, message);

    }

    public static void warning(String TAG, String message) {
        Log.w(TAG, message);

    }

    public static void info(String TAG, String message) {
        Log.i(TAG, message);
    }

    public static void debug(String TAG, String message) {
        if (BackendStorage.getInstance().isExLog()) {
            Log.d(TAG, message);
        }
    }

    public static void pdebug(String TAG, String message) {
        Log.d(TAG, message);
    }
}
