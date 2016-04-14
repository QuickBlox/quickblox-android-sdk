package com.quickblox.simplesample.messages;

import com.quickblox.sample.core.CoreApp;

public class App extends CoreApp {

    private static App instance;

    private int currentUserId;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

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