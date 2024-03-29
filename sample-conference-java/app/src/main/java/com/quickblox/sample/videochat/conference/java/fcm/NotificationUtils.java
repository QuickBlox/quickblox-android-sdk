package com.quickblox.sample.videochat.conference.java.fcm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.quickblox.sample.videochat.conference.java.utils.FcmConsts;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

class NotificationUtils {
    private static final String CHANNEL_ONE_ID = "QuickBlox Conference Chat Channel";// The id of the channel.
    private static final String CHANNEL_ONE_NAME = "Conference Chat Channel";

    public static void showNotification(Context context, Class<? extends Activity> activityClass,
                                        String title, String message, @DrawableRes int icon,
                                        int notificationId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            createChannelIfNotExist(notificationManager);
        }
        Notification notification = buildNotification(context, activityClass, title, message, icon);

        if (notificationManager != null) {
            notificationManager.notify(notificationId, notification);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createChannelIfNotExist(NotificationManager notificationManager) {
        if (notificationManager.getNotificationChannel(CHANNEL_ONE_ID) == null) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private static Notification buildNotification(Context context, Class<? extends Activity> activityClass,
                                                  String title, String message, @DrawableRes int icon) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        return new NotificationCompat.Builder(context, CHANNEL_ONE_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(buildContentIntent(context, activityClass, message))
                .build();
    }

    private static PendingIntent buildContentIntent(Context context, Class<? extends Activity> activityClass, String message) {
        Intent intent = new Intent(context, activityClass);
        intent.putExtra(FcmConsts.EXTRA_FCM_MESSAGE, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int intentFlag = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intentFlag = PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(context, 0, intent, intentFlag);
    }
}