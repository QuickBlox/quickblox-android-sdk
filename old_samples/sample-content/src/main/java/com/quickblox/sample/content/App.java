package com.quickblox.sample.content;

import com.crashlytics.android.Crashlytics;
import com.quickblox.sample.core.CoreApp;

import io.fabric.sdk.android.Fabric;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }
}