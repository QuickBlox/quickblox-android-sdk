package com.quickblox.sample.chat.kotlin.managers

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper

class BackgroundListener : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    internal fun onBackground() {
        ChatHelper.destroy()
        Log.d("BackgroundListener", "Background Successful")
    }
}