package com.quickblox.sample.videochat.conference.java.managers;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

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
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        foreground = true;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        foreground = false;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}