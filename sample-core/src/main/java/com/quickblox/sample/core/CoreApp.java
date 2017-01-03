package com.quickblox.sample.core;

import android.app.Application;
import android.text.TextUtils;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.ServiceZone;
import com.quickblox.sample.core.models.QbConfigs;
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;

import java.io.IOException;

public class CoreApp extends Application {

    private static CoreApp instance;
    private static final String CORE_APP_CONFIG_FILE_NAME = "qb_config.json";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initCredentials();
    }

    public static synchronized CoreApp getInstance() {
        return instance;
    }

    //TODO VT maybe need change for init configs for all modules with own config file
    public void initCredentials(){
        QbConfigs qbConfigs;
        try {
            qbConfigs = CoreConfigUtils.getCoreConfigs(CORE_APP_CONFIG_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        QBSettings.getInstance().init(getApplicationContext(), qbConfigs.getAppId(), qbConfigs.getAuthKey(), qbConfigs.getAuthSecret());
        QBSettings.getInstance().setAccountKey(qbConfigs.getAccountKey());

        if (!TextUtils.isEmpty(qbConfigs.getApiDomain()) && !TextUtils.isEmpty(qbConfigs.getChatDomain())){
            QBSettings.getInstance().setEndpoints(qbConfigs.getApiDomain(), qbConfigs.getChatDomain(), ServiceZone.PRODUCTION);
            QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
        }
    }
}