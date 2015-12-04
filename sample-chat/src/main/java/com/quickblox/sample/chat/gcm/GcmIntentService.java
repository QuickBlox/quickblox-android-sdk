package com.quickblox.sample.chat.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.activity.DialogsActivity;
import com.quickblox.sample.chat.utils.Consts;

public class GcmIntentService extends IntentService {
    private static final String TAG = GcmIntentService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 1;

    public GcmIntentService() {
        super(Consts.GCM_INTENT_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent()");

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        Bundle extras = intent.getExtras();

        if (!extras.isEmpty()) {
            // Filter messages based on the message type. Since it is likely that GCM
            // will be extended in the future with new message types, just ignore any
            // message types you're not interested in, or that you don't recognize
            String readableMessageType = null;
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                readableMessageType = Consts.GCM_SEND_ERROR;
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                readableMessageType = Consts.GCM_DELETED_MESSAGE;
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                readableMessageType = Consts.GCM_RECEIVED;
            }

            showNotificationFromGcm(readableMessageType, extras);
        }
        // Release the wakelock provided by the GcmBroadcastReceiver earlier
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might
    // choose to do with received GCM message
    private void showNotificationFromGcm(String type, Bundle extras) {
        String messageText = extras.getString(Consts.EXTRA_GCM_MESSAGE);
        Log.i(TAG, "Received push. Type=" + type + " with data: " + extras.toString());

        Intent intent = new Intent(this, DialogsActivity.class);
        intent.putExtra(Consts.EXTRA_GCM_MESSAGE, messageText);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(Consts.GCM_NOTIFICATION)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageText))
                .setContentText(messageText)
                .setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        Intent gcmBroadcastIntent = new Intent(Consts.ACTION_NEW_GCM_EVENT);
        gcmBroadcastIntent.putExtra(Consts.EXTRA_GCM_MESSAGE, messageText);
        LocalBroadcastManager.getInstance(this).sendBroadcast(gcmBroadcastIntent);
    }
}