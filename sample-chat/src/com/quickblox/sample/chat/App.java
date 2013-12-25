package com.quickblox.sample.chat;

import android.app.Application;

import com.quickblox.module.users.model.QBUser;

public class App extends Application {

    private static App instance;
    private QBUser qbUser;

    public static synchronized App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public QBUser getQbUser() {
        return qbUser;
    }

    public void setQbUser(QBUser qbUser) {
        this.qbUser = qbUser;
    }
}
