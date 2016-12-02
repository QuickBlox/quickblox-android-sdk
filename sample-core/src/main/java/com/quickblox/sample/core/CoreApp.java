package com.quickblox.sample.core;

import android.app.Application;
import android.text.TextUtils;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.ServiceZone;
import com.quickblox.sample.core.models.CoreConfigs;
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;

public class CoreApp extends Application {

    private static CoreApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initCredentials();
    }

    public static synchronized CoreApp getInstance() {
        return instance;
    }

    public void initCredentials(){
        CoreConfigs coreConfigs = CoreConfigUtils.getCoreConfigs();
        QBSettings.getInstance().init(getApplicationContext(), coreConfigs.getAppId(), coreConfigs.getAuthKey(), coreConfigs.getAuthSecret());
        QBSettings.getInstance().setAccountKey(coreConfigs.getAccountKey());

        if (!TextUtils.isEmpty(coreConfigs.getApiDomain()) && !TextUtils.isEmpty(coreConfigs.getChatDomain())){
            QBSettings.getInstance().setEndpoints(coreConfigs.getApiDomain(), coreConfigs.getChatDomain(), ServiceZone.PRODUCTION);
            QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
        }
    }
}