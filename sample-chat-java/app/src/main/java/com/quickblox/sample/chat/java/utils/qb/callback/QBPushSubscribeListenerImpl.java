package com.quickblox.sample.chat.java.utils.qb.callback;

import android.util.Log;

import com.quickblox.messages.services.QBPushManager;


public class QBPushSubscribeListenerImpl implements QBPushManager.QBSubscribeListener {

    @Override
    public void onSubscriptionCreated() {
        Log.d("Subscription Listener", "Subscription Created");
    }

    @Override
    public void onSubscriptionError(Exception e, int i) {

    }

    @Override
    public void onSubscriptionDeleted(boolean b) {

    }
}