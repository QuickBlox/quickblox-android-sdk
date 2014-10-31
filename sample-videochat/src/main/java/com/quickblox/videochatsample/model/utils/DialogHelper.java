package com.quickblox.videochatsample.model.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import com.quickblox.videochat.model.definition.VideoChatConstants;
import com.quickblox.videochatsample.R;
import com.quickblox.videochatsample.model.listener.OnCallDialogListener;

public class DialogHelper {

    private static AlertDialog.Builder builder;

    public static AlertDialog showCallDialog(Context context, final OnCallDialogListener callDialogListener) {
        if (builder == null) {
            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            callDialogListener.onAcceptCallClick();
                            deleteCallDialog();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            callDialogListener.onRejectCallClick();
                            deleteCallDialog();
                            break;
                    }
//                    dismissDialog();
                }
            };
            builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.calling_dialog_title))
                    .setMessage(R.string.calling_dialog_txt)
                    .setPositiveButton(VideoChatConstants.YES, onClickListener)
                    .setNegativeButton(VideoChatConstants.NO, onClickListener)
                    .show();
        }

        return builder.create();
    }


    private static void deleteCallDialog() {
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                builder = null;
            }
        }, 2000);
    }

    public static void dismissDialog() {
        builder = null;
    }
}
