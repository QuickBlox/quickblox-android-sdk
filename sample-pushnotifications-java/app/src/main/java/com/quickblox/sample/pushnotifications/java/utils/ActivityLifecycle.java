package com.quickblox.sample.pushnotifications.java.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.quickblox.sample.pushnotifications.java.App;

public class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    private static ActivityLifecycle instance;

    private boolean foreground = false;

    public static void init() {
        if (instance == null) {
            instance = new ActivityLifecycle();
            App.getInstance().registerActivityLifecycleCallbacks(instance);
        }
    }

    public static synchronized ActivityLifecycle getInstance() {
        return instance;
    }

    public boolean isForeground() {
        return foreground;
    }

    public boolean isBackground() {
        return !foreground;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // empty
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // empty
    }

    @Override
    public void onActivityResumed(Activity activity) {
        foreground = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        foreground = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // empty
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // empty
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // empty
    }
}