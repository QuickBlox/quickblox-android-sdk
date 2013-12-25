package com.quickblox.sample.chat;

import android.app.Application;

import com.quickblox.module.users.model.QBUser;

public class QuickbloxSampleChat extends Application {

    private static QuickbloxSampleChat instance;
    private QBUser qbUser;

    public static synchronized QuickbloxSampleChat getInstance() {
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
