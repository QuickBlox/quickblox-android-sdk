package com.quickblox.sample.core.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;

import com.quickblox.sample.core.R;

import java.util.List;

public class ErrorUtils {

    private static Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private ErrorUtils() {}

    public static void showErrorDialog(final Context context, final String errorMessage, final List<String> errors) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.dlg_error)
                        .setMessage(errorMessage + errors)
                        .create()
                        .show();
            }
        });
    }
}
