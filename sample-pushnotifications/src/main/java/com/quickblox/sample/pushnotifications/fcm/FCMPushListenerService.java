package com.quickblox.sample.pushnotifications.fcm;

import com.google.firebase.messaging.RemoteMessage;
import com.quickblox.messages.services.fcm.QBFcmPushListenerService;
import com.quickblox.sample.pushnotifications.App;

import java.util.Map;

/**
 * Created by egor on 10/9/18.
 */

public class FCMPushListenerService extends QBFcmPushListenerService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (App.getInstance().isFcmEnabled()) {
            super.onMessageReceived(remoteMessage);
        }
    }

    @Override
    protected void sendPushMessage(Map data, String from, String message) {
        super.sendPushMessage(data, from, message);
    }
}
