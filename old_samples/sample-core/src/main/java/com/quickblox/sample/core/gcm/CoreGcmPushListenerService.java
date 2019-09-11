package com.quickblox.sample.core.gcm;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.messages.services.gcm.QBGcmPushListenerService;
import com.quickblox.sample.core.utils.ActivityLifecycle;

public abstract class CoreGcmPushListenerService extends QBGcmPushListenerService {
    private static final String TAG = CoreGcmPushListenerService.class.getSimpleName();

    @Override
    public void sendPushMessage(Bundle data, String from, String message) {
        super.sendPushMessage(data, from, message);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);

        if (ActivityLifecycle.getInstance().isBackground()) {
            showNotification(message);
        }
    }

    protected abstract void showNotification(String message);
}