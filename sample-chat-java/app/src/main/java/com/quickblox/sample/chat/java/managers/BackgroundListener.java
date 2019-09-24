package com.quickblox.sample.chat.java.managers;

import android.util.Log;

import com.quickblox.sample.chat.java.utils.chat.ChatHelper;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class BackgroundListener implements LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onBackground() {
        ChatHelper.getInstance().destroy();
        Log.d("BackgroundListener", "Background Successful");
    }
}