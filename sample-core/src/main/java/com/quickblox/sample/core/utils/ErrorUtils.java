package com.quickblox.sample.core.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.R;

import java.util.List;

public class ErrorUtils {

    private static final String NO_CONNECTION_ERROR = "Connection failed. Please check your internet connection.";
    private static final String NO_RESPONSE_TIMEOUT = "No response received within reply timeout.";
    private static Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private ErrorUtils() {
    }

    public static Snackbar showSnackbar(View view, @StringRes int errorMessage, Exception e,
                                        @StringRes int actionLabel, View.OnClickListener clickListener) {
        String error = (e == null) ? "" : e.getMessage();
        boolean noConnection = NO_CONNECTION_ERROR.equals(error);
        boolean timeout = error.startsWith(NO_RESPONSE_TIMEOUT);
        if (noConnection || timeout) {
            return showSnackbar(view, R.string.no_internet_connection, actionLabel, clickListener);
        } else if (errorMessage == 0) {
            return showSnackbar(view, error, actionLabel, clickListener);
        } else if (error.equals("")) {
            return showSnackbar(view, errorMessage, CoreApp.getInstance().getString(R.string.no_internet_connection), actionLabel, clickListener);
        } else {
            return showSnackbar(view, errorMessage, error, actionLabel, clickListener);
        }
    }

    public static Snackbar showSnackbar(View view, @StringRes int errorMessage, String error,
                                        @StringRes int actionLabel, View.OnClickListener clickListener) {
        String errorMessageString = CoreApp.getInstance().getString(errorMessage);
        String message = String.format("%s: %s", errorMessageString, error);
        return showSnackbar(view, message, actionLabel, clickListener);
    }

    public static Snackbar showSnackbar(View view, @StringRes int message,
                                        @StringRes int actionLabel,
                                        View.OnClickListener clickListener) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(actionLabel, clickListener);
        snackbar.show();
        return snackbar;
    }

    private static Snackbar showSnackbar(View view, String message,
                                         @StringRes int actionLabel,
                                         View.OnClickListener clickListener) {
        Snackbar snackbar = Snackbar.make(view, message.trim(), Snackbar.LENGTH_INDEFINITE);
        if (clickListener != null) {
            snackbar.setAction(actionLabel, clickListener);
        }
        snackbar.show();
        return snackbar;
    }

    public static void showErrorToast(QBResponseException exception) {
        Toaster.shortToast(String.format("[ERROR] Request has been completed with errors: %s", exception.getErrors()
                + ", code: " + exception.getHttpStatusCode()));
    }

    public static void showErrorDialog(Context context, @StringRes int errorMessage, String error) {
        showErrorDialog(context, context.getString(errorMessage), error);
    }

    public static void showErrorDialog(Context context, @StringRes int errorMessage, List<String> errors) {
        showErrorDialog(context, context.getString(errorMessage), errors.toString());
    }

    private static void showErrorDialog(Context context, String errorMessage, String error) {
        showErrorDialog(context, String.format("%s: %s", errorMessage, error));
    }

    private static void showErrorDialog(final Context context, final String errorMessage) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.dlg_error)
                        .setMessage(errorMessage)
                        .create()
                        .show();
            }
        });
    }
}
