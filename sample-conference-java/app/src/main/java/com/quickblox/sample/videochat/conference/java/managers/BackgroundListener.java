package com.quickblox.sample.videochat.conference.java.managers;

import android.util.Log;

import com.quickblox.chat.QBChatService;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class BackgroundListener implements LifecycleObserver {
    private static final String TAG = BackgroundListener.class.getSimpleName();

    public BackgroundListener() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onBackground() {
        QBChatService.getInstance().destroy();
        Log.d(TAG, "Going Background");
    }
}