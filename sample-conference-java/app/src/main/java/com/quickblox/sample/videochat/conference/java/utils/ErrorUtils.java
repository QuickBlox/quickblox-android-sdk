package com.quickblox.sample.videochat.conference.java.utils;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.quickblox.sample.videochat.conference.java.R;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

public class ErrorUtils {

    private ErrorUtils() {
    }

    public static Snackbar showSnackbar(Context context, View view, @StringRes int errorMessageResource, Exception e,
                                        @StringRes int actionLabel, View.OnClickListener clickListener) {
        String noConnectionError = context.getResources().getString(R.string.error_no_connection);
        String noResponseTimeout = context.getResources().getString(R.string.error_no_responce_timeout);
        String noServerConnection = context.getResources().getString(R.string.no_server_connection);
        String error = (e == null) ? "" : e.getMessage();
        boolean noConnection = noConnectionError.equals(error);
        boolean timeout = false;
        if (error != null) {
            timeout = error.startsWith(noResponseTimeout);
        }

        /*if (noConnection || timeout) {
            return showSnackbar(context, view, noServerConnection, actionLabel, clickListener);
        } else*/
        if (errorMessageResource == 0) {
            return showSnackbar(context, view, error, actionLabel, clickListener);
        } else if (error.equals("")) {
            return showSnackbar(context, view, errorMessageResource, noServerConnection, actionLabel, clickListener);
        } else {
            return showSnackbar(context, view, errorMessageResource, error, actionLabel, clickListener);
        }
    }

    private static Snackbar showSnackbar(Context context, View view, @StringRes int errorMessage, String error,
                                         @StringRes int actionLabel, View.OnClickListener clickListener) {
        String errorMessageString = context.getResources().getString(errorMessage);
        String message = String.format("%s: %s", errorMessageString, error);
        return showSnackbar(context, view, message, actionLabel, clickListener);
    }

    public static Snackbar showInfoSnackbar(Context context, View view, String message,
                                            @StringRes int actionLabel, View.OnClickListener clickListener) {
        return showSnackbar(context, view, message, actionLabel, clickListener);
    }

    private static Snackbar showSnackbar(Context context, View view, String message,
                                         @StringRes int actionLabel,
                                         View.OnClickListener clickListener) {
        Snackbar snackbar = Snackbar.make(view, message.trim(), Snackbar.LENGTH_INDEFINITE);
        if (clickListener != null) {

            snackbar.setAction(actionLabel, clickListener);
            snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.color_blue_qb));
            TextView tv = snackbar.getView().findViewById(R.id.snackbar_text);
            tv.setMaxLines(8);
            tv.setTextColor(ContextCompat.getColor(context, R.color.color_light_blue_qb));
        }
        snackbar.show();
        return snackbar;
    }
}