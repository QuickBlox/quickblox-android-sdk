package com.quickblox.sample.pushnotifications.kotlin.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle

object ActivityLifecycle : Application.ActivityLifecycleCallbacks {

    private var isForeground: Boolean = false

    fun isBackground(): Boolean {
        return !isForeground
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // empty
    }

    override fun onActivityStarted(activity: Activity) {
        // empty
    }

    override fun onActivityResumed(activity: Activity) {
        isForeground = true
    }

    override fun onActivityPaused(activity: Activity) {
        isForeground = false
    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // empty
    }

    override fun onActivityDestroyed(activity: Activity) {
        // empty
    }
}