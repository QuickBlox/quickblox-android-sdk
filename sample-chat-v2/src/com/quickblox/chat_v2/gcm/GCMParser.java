package com.quickblox.chat_v2.gcm;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.ui.activities.ChatActivity;


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
        Log.d("INcomne push", "push =  "+extras);

        Vibrator vibrato = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrato.vibrate(1000);
        test(context);
    }

    public void test(Context context){
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder newNotify = new NotificationCompat.Builder(context)
                .setContentTitle("You have unread QB chats")
                .setContentText("You have unread QB chats")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher);
        newNotify.setContentIntent(PendingIntent.getActivity(context,0,new Intent(context, ChatActivity.class),0));
        nm.notify(0, newNotify.build());
    }
}
