package com.quickblox.sample.chat.kotlin.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle


object ActivityLifecycle : Application.ActivityLifecycleCallbacks {
    private var foreground = false

    fun isBackground(): Boolean {
        return !foreground
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
        foreground = true
    }

    override fun onActivityPaused(activity: Activity) {
        foreground = false
    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }
}