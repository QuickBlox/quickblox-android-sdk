package com.quickblox.simplesample.messages.main.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class DialogUtils {

    private static Toast toast;

    public static void show(Context context, String message) {
        if (message == null) {
            return;
        }
        if (toast == null && context != null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        }
        if (toast != null) {
            toast.setText(message);
            toast.show();
        }
    }

    public static void showLong(Context context, String message) {
        if (message == null) {
            return;
        }
        if (toast == null && context != null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        }
        if (toast != null) {
            toast.setText(message);
            toast.show();
        }
    }

    public static AlertDialog createAlertDialog(Context context, int titleId, CharSequence[] itemsArray, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(titleId));
        builder.setItems(itemsArray, onClickListener);
        return builder.create();
    }
}