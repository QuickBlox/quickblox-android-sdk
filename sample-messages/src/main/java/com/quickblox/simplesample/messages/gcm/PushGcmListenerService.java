package com.quickblox.simplesample.messages.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.quickblox.simplesample.messages.Consts;
import com.quickblox.simplesample.messages.R;
import com.quickblox.simplesample.messages.activities.MessagesActivity;

public class PushGcmListenerService extends GcmListenerService {
    private final String TAG = getClass().getSimpleName();
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE = 1;

    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(Consts.EXTRA_GCM_MESSAGE);
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        sendNotification(message);
    }
    /**
     * Create and show a simple notification containing the received GCM message.
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MessagesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(Consts.GCM_NOTIFICATION)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

        Intent gcmBroadcastIntent = new Intent(Consts.ACTION_NEW_GCM_EVENT);
        gcmBroadcastIntent.putExtra(Consts.EXTRA_GCM_MESSAGE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(gcmBroadcastIntent);
    }
}