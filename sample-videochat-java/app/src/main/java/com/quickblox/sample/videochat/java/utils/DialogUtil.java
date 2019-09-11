package com.quickblox.sample.videochat.java.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * QuickBlox team
 */
public class DialogUtil {

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showToast(Context context, int messageId) {
        Toast.makeText(context, context.getString(messageId), Toast.LENGTH_LONG).show();
    }
}