package com.quickblox.simplesample.messages.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quickblox.sample.core.gcm.CoreGcmPushListenerService;
import com.quickblox.sample.core.utils.NotificationUtils;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.sample.core.utils.constant.GcmConsts;
import com.quickblox.simplesample.messages.ActivityLifecycle;
import com.quickblox.simplesample.messages.R;
import com.quickblox.simplesample.messages.activities.SplashActivity;

public class GcmPushListenerService extends CoreGcmPushListenerService {
    private static final String TAG = GcmPushListenerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(GcmConsts.EXTRA_GCM_MESSAGE);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);

        if (ActivityLifecycle.get().isBackground()) {
            showNotification(message);
        }
        sendPushMessageBroadcast(message);
    }

    @Override
    protected void showNotification(String message) {
        NotificationUtils.showNotification(this, SplashActivity.class,
                ResourceUtils.getString(R.string.notification_title), message,
                R.mipmap.ic_launcher, NOTIFICATION_ID);
    }

    @Override
    protected void sendPushMessageBroadcast(String message) {
        Intent gcmBroadcastIntent = new Intent(GcmConsts.ACTION_NEW_GCM_EVENT);
        gcmBroadcastIntent.putExtra(GcmConsts.EXTRA_GCM_MESSAGE, message);

        LocalBroadcastManager.getInstance(this).sendBroadcast(gcmBroadcastIntent);
    }

}