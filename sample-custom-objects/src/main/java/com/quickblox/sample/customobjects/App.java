package com.quickblox.sample.customobjects;

import com.crashlytics.android.Crashlytics;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.customobjects.utils.Consts;
import io.fabric.sdk.android.Fabric;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }
}