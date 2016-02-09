package com.quickblox.simplesample.messages.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.quickblox.sample.core.utils.NotificationUtils;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.simplesample.messages.Consts;
import com.quickblox.simplesample.messages.R;
import com.quickblox.simplesample.messages.activities.SplashActivity;

public class GcmPushListenerService extends GcmListenerService {
    private final String TAG = getClass().getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(Consts.EXTRA_GCM_MESSAGE);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);

        showNotification(message);
        sendPushMessageBroadcast(message);
    }

    private void showNotification(String message) {
        NotificationUtils.showNotification(this, SplashActivity.class,
                ResourceUtils.getString(R.string.notification_title), message,
                R.mipmap.ic_launcher, NOTIFICATION_ID);
    }

    private void sendPushMessageBroadcast(String message) {
        Intent gcmBroadcastIntent = new Intent(Consts.ACTION_NEW_GCM_EVENT);
        gcmBroadcastIntent.putExtra(Consts.EXTRA_GCM_MESSAGE, message);

        LocalBroadcastManager.getInstance(this).sendBroadcast(gcmBroadcastIntent);
    }
}