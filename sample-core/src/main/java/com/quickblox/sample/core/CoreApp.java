package com.quickblox.sample.core;

import android.app.Application;
import android.text.TextUtils;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.ServiceZone;
import com.quickblox.sample.core.models.CoreAppConfigs;
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
        CoreAppConfigs coreAppConfigs = CoreConfigUtils.getCoreConfigs();
        QBSettings.getInstance().init(getApplicationContext(), coreAppConfigs.getAppId(), coreAppConfigs.getAuthKey(), coreAppConfigs.getAuthSecret());
        QBSettings.getInstance().setAccountKey(coreAppConfigs.getAccountKey());

        if (!TextUtils.isEmpty(coreAppConfigs.getApiDomain()) && !TextUtils.isEmpty(coreAppConfigs.getChatDomain())){
            QBSettings.getInstance().setEndpoints(coreAppConfigs.getApiDomain(), coreAppConfigs.getChatDomain(), ServiceZone.PRODUCTION);
            QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
        }
    }
}