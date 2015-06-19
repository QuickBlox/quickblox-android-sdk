package com.quickblox.sample.chat.pushnotifications;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.activities.DialogsActivity;

public class GCMIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;

    private static final String TAG = GCMIntentService.class.getSimpleName();

    private NotificationManager notificationManager;

    public GCMIntentService() {
        super(Consts.GCM_INTENT_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "new push");

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = googleCloudMessaging.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                processNotification(Consts.GCM_SEND_ERROR, extras);
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                processNotification(Consts.GCM_DELETED_MESSAGE, extras);
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                processNotification(Consts.GCM_RECEIVED, extras);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void processNotification(String type, Bundle extras) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final String messageValue = extras.getString("message");

        Intent intent = new Intent(this, DialogsActivity.class);
        intent.putExtra(Consts.EXTRA_MESSAGE, messageValue);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(
                R.drawable.app_icon).setContentTitle(Consts.GCM_NOTIFICATION).setStyle(
                new NotificationCompat.BigTextStyle().bigText(messageValue)).setContentText(messageValue);

        mBuilder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());


        // notify about new push
        //
        Intent intentNewPush = new Intent(Consts.NEW_PUSH_EVENT);
        intentNewPush.putExtra(Consts.EXTRA_MESSAGE, messageValue);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentNewPush);

        Log.i(TAG, "Broadcasting event " + Consts.NEW_PUSH_EVENT + " with data: " + messageValue);
    }
}