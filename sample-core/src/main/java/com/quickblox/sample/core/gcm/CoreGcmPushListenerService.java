package com.quickblox.sample.core.gcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.quickblox.sample.core.utils.ActivityLifecycle;

import java.util.Map;

public abstract class CoreGcmPushListenerService extends FirebaseMessagingService {
    private static final String TAG = CoreGcmPushListenerService.class.getSimpleName();
    private static final String MESSAGE_KEY = "message";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Map data = remoteMessage.getData();

        String message = (String) data.get(MESSAGE_KEY);
        Log.v(TAG, "data: " + data);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);

        if (ActivityLifecycle.getInstance().isBackground()) {
            showNotification(message);
        }

        sendPushMessageBroadcast(message);
    }

    protected abstract void showNotification(String message);

    protected abstract void sendPushMessageBroadcast(String message);
}