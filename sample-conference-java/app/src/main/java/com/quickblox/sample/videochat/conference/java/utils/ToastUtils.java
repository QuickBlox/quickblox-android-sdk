package com.quickblox.sample.videochat.conference.java.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;

public class ToastUtils {

    private ToastUtils() {
    }

    public static void shortToast(Context context, String message) {
        show(context, message, Toast.LENGTH_LONG);
    }

    public static void shortToast(Context context, @StringRes int resource) {
        show(context, context.getString(resource), Toast.LENGTH_SHORT);
    }

    public static void longToast(Context context, String message) {
        show(context, message, Toast.LENGTH_LONG);
    }

    public static void longToast(Context context, @StringRes int resource) {
        show(context, context.getString(resource), Toast.LENGTH_LONG);
    }

    private static void show(Context context, String message, int length) {
        Toast.makeText(context, message, length).show();
    }
}