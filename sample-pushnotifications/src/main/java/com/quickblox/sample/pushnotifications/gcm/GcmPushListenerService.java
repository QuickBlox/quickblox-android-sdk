package com.quickblox.sample.pushnotifications.gcm;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.messages.services.gcm.QBGcmPushListenerService;
import com.quickblox.sample.core.utils.ActivityLifecycle;
import com.quickblox.sample.core.utils.NotificationUtils;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.sample.pushnotifications.App;
import com.quickblox.sample.pushnotifications.R;
import com.quickblox.sample.pushnotifications.activities.SplashActivity;

public class GcmPushListenerService extends QBGcmPushListenerService {
    private static final String TAG = GcmPushListenerService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 1;

    @Override
    public void sendPushMessage(Bundle data, String from, String message) {
        if (App.getInstance().isGcmEnabled()) {
            super.sendPushMessage(data, from, message);

            Log.v(TAG, "From: " + from);
            Log.v(TAG, "Message: " + message);

            if (ActivityLifecycle.getInstance().isBackground()) {
                showNotification(message);
            }
        }
    }

    protected void showNotification(String message) {
        NotificationUtils.showNotification(this, SplashActivity.class,
                ResourceUtils.getString(R.string.notification_title), message,
                R.mipmap.ic_notification, NOTIFICATION_ID);
    }
}