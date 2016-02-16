package com.quickblox.simplesample.messages;

import com.quickblox.core.QBSettings;
import com.quickblox.sample.core.CoreApp;

public class App extends CoreApp {

    private static App instance;
    private int currentUserId;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        QBSettings.getInstance().fastConfigInit(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET);
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