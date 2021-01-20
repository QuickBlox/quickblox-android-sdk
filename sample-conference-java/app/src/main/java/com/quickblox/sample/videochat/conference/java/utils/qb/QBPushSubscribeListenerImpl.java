package com.quickblox.sample.videochat.conference.java.utils.qb;

import android.util.Log;

import com.quickblox.messages.services.QBPushManager;


public class QBPushSubscribeListenerImpl implements QBPushManager.QBSubscribeListener {
    private static final String TAG = QBPushSubscribeListenerImpl.class.getSimpleName();

    @Override
    public void onSubscriptionCreated() {
        Log.d(TAG, "Subscription Created");
    }

    @Override
    public void onSubscriptionError(Exception e, int i) {
        Log.d(TAG, "Subscription Error - " + e.getMessage() );
    }

    @Override
    public void onSubscriptionDeleted(boolean b) {
        Log.d(TAG, "Subscription Deleted");
    }
}