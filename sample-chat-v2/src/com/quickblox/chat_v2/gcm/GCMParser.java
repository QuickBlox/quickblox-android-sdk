package com.quickblox.chat_v2.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;


/**
 * Created with IntelliJ IDEA.
 * User: Nikolay Dymura
 * Date: 5/27/13
 * E-mail: nikolay.dymura@gmail.com
 */
public final class GCMParser {

    private GCMParser() {

    }

    public static GCMParser newInstance() {
        return new GCMParser();
    }

    public void parseMessage(Context context, Bundle extras) {
//        if (extras != null) {
//            GCMMessage message = GCMMessage.valueOf(extras);
//            if (message != null) {
//
//            }
//        }
    }

    private void handleMissedCall(Context context, GCMMessage message) {

    }

    public void showNotification(Context pContext, String title, String text, Intent pTarget) {

    }

}
