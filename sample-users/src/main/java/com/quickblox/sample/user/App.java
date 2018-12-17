package com.quickblox.sample.user;

import com.crashlytics.android.Crashlytics;
import com.quickblox.sample.core.CoreApp;

import io.fabric.sdk.android.Fabric;

public class App extends CoreApp {
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }
}