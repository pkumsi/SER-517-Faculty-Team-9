package com.example.carma_android_app.utils;

import android.util.Log;

/**
 * Centralized logging utility Provides consistent logging across the
 * application
 */
public class Logger {

    private static final String TAG = "CARMA";
    private static final boolean DEBUG = true; // Set to false for production

    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(TAG + "_" + tag, message);
        }
    }

    public static void i(String message) {
        Log.i(TAG, message);
    }

    public static void i(String tag, String message) {
        Log.i(TAG + "_" + tag, message);
    }

    public static void w(String message) {
        Log.w(TAG, message);
    }

    public static void w(String tag, String message) {
        Log.w(TAG + "_" + tag, message);
    }

    public static void e(String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
    }

    public static void e(String tag, String message, Throwable throwable) {
        Log.e(TAG + "_" + tag, message, throwable);
    }

    public static void e(String message) {
        Log.e(TAG, message);
    }
}
