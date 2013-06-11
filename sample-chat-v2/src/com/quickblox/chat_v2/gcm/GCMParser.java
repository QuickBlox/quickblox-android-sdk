package com.quickblox.chat_v2.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.quickblox.chat_v2.R;


/**
 * Created with IntelliJ IDEA.
 * User: Nikolay Dymura
 * Date: 5/27/13
 * E-mail: nikolay.dymura@gmail.com
 */
public final class GCMParser {

    private NotificationManager nm;

    private GCMParser() {
    }

    public static GCMParser newInstance() {
        return new GCMParser();
    }

    public void parseMessage(Context context, Bundle extras) {
        if (extras != null) {
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification noti = new Notification.Builder(context)
                    .setContentTitle("test title").build();
        }
    }
}
