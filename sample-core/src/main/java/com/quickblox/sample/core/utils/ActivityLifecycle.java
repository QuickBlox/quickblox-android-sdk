package com.quickblox.sample.core.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {

    private static ActivityLifecycle instance;

    private boolean foreground = false;

    public static void init(Application app) {
        if (instance == null) {
            instance = new ActivityLifecycle();
            app.registerActivityLifecycleCallbacks(instance);
        }
    }

    private ActivityLifecycle() {
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

    }

    @Override
    public void onActivityStarted(Activity activity) {

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

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
