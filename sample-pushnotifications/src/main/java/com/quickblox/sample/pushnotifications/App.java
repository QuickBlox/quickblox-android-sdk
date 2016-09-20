package com.quickblox.sample.pushnotifications;

import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.utils.ActivityLifecycle;
import com.quickblox.sample.pushnotifications.utils.Consts;

public class App extends CoreApp {

    private static App instance;

    private int currentUserId;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ActivityLifecycle.init(this);

        initCredentials(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET, Consts.QB_ACCOUNT_KEY);
    }

    public static synchronized App getInstance() {
        return instance;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
    }
}