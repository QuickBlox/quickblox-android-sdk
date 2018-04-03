package com.quickblox.sample.conference;

import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.quickblox.conference.ConferenceConfig;
import com.quickblox.sample.conference.util.QBResRequestExecutor;
import com.quickblox.sample.core.CoreApp;

import io.fabric.sdk.android.Fabric;

public class App extends CoreApp {
    private static App instance;
    private QBResRequestExecutor qbResRequestExecutor;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        initApplication();
        initConferenceConfig();
        checkMultiServer();
    }

    private void initApplication() {
        instance = this;
    }

    private void initConferenceConfig() {
        if (!TextUtils.isEmpty(qbConfigs.getJanusServerUrl())) {
            ConferenceConfig.setUrl(qbConfigs.getJanusServerUrl());
        }
        if (!TextUtils.isEmpty(qbConfigs.getJanusProtocol())) {
            ConferenceConfig.setProtocol(qbConfigs.getJanusProtocol());
        }
        if (!TextUtils.isEmpty(qbConfigs.getJanusPlugin())) {
            ConferenceConfig.setPlugin(qbConfigs.getJanusPlugin());
        }
    }

    private void checkMultiServer() {
        if (ConferenceConfig.getUrl() == null) {
            throw new AssertionError(getString(R.string.error_server_url_null));
        }
    }

    public synchronized QBResRequestExecutor getQbResRequestExecutor() {
        return qbResRequestExecutor == null
                ? qbResRequestExecutor = new QBResRequestExecutor()
                : qbResRequestExecutor;
    }
}
