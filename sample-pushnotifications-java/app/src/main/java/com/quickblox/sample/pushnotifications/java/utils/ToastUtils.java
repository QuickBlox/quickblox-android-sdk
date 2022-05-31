package com.quickblox.sample.pushnotifications.java.utils;

import android.widget.Toast;

import androidx.annotation.StringRes;

import com.quickblox.sample.pushnotifications.java.App;

public class ToastUtils {

    private ToastUtils() {
    }

    public static void shortToast(String message) {
        show(message, Toast.LENGTH_LONG);
    }

    public static void shortToast(@StringRes int resource) {
        show(App.getInstance().getString(resource), Toast.LENGTH_SHORT);
    }

    public static void longToast(String message) {
        show(message, Toast.LENGTH_LONG);
    }

    public static void longToast(@StringRes int resource) {
        show(App.getInstance().getString(resource), Toast.LENGTH_LONG);
    }

    private static void show(String message, int length) {
        Toast.makeText(App.getInstance(), message, length).show();
    }
}