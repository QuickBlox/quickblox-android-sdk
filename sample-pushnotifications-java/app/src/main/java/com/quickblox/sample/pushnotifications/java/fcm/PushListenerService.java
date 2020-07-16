package com.quickblox.sample.pushnotifications.java.fcm;

import android.util.Log;

import com.quickblox.messages.services.fcm.QBFcmPushListenerService;
import com.quickblox.sample.pushnotifications.java.R;
import com.quickblox.sample.pushnotifications.java.activities.SplashActivity;
import com.quickblox.sample.pushnotifications.java.utils.ActivityLifecycle;
import com.quickblox.sample.pushnotifications.java.utils.NotificationUtils;

import java.util.Map;

public class PushListenerService extends QBFcmPushListenerService {
    private static final String TAG = PushListenerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    protected void showNotification(String message) {
        NotificationUtils.showNotification(this, SplashActivity.class,
                getString(R.string.notification_title), message,
                R.mipmap.ic_notification, NOTIFICATION_ID);
    }

    @Override
    protected void sendPushMessage(Map data, String from, String message) {
        super.sendPushMessage(data, from, message);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);

        if (ActivityLifecycle.getInstance().isBackground()) {
            showNotification(message);
        }
    }
}