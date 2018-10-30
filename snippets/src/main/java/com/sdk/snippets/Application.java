package com.sdk.snippets;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.ServiceZone;
import com.quickblox.sample.core.CoreApp;
import com.sdk.snippets.core.ApplicationConfig;


public class Application extends CoreApp {
    @Override
    public void onCreate() {
        super.onCreate();

        ApplicationConfig.init(getApplicationContext());

        ApplicationConfig CONFIG = ApplicationConfig.getInstance();

        // App credentials from QB Admin Panel
        QBSettings.getInstance().init(getApplicationContext(), CONFIG.getAppId(),
                CONFIG.getAuthKey(), CONFIG.getAuthSecret());

        if (CONFIG.getApiDomain().equals("https://api.quickblox.com")) {
            QBSettings.getInstance().setAccountKey(CONFIG.getAccountKey());
        } else {
            // specify custom domains
            QBSettings.getInstance().setEndpoints(CONFIG.getApiDomain(), CONFIG.getChatDomain(), ServiceZone.PRODUCTION);
            QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
        }
    }
}