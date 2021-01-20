package com.quickblox.sample.videochat.conference.java.fcm;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.quickblox.messages.services.fcm.QBFcmPushListenerService;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.activities.SplashActivity;
import com.quickblox.sample.videochat.conference.java.managers.ActivityLifecycle;

import java.util.Map;

public class PushListenerService extends QBFcmPushListenerService {
    private static final String TAG = PushListenerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    protected void showNotification(String message) {
        NotificationUtils.showNotification(getApplicationContext(), SplashActivity.class,
                getApplicationContext().getString(R.string.chat_notification_title), message,
                R.drawable.ic_logo_vector, NOTIFICATION_ID);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    @Override
    protected void sendPushMessage(Map data, String from, String message) {
        super.sendPushMessage(data, from, message);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);
        Log.v(TAG, "Data Map: " + data);
        if (ActivityLifecycle.getInstance().isBackground()) {
            showNotification(message);
        }
    }
}