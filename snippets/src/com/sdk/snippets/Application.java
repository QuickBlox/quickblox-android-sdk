package com.sdk.snippets;

import com.quickblox.core.LogLevel;
import com.quickblox.core.QBSettings;
import com.quickblox.core.ServiceZone;
import com.sdk.snippets.core.ApplicationConfig;

/**
 * Created by igorkhomenko on 11/20/15.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ApplicationConfig.init(getApplicationContext());

        ApplicationConfig CONFIG = ApplicationConfig.getInstance();

        // App credentials from QB Admin Panel
        QBSettings.getInstance().init(getApplicationContext(), CONFIG.getAppId(),
                CONFIG.getAuthKey(), CONFIG.getAuthSecret());

//        QBSettings.getInstance().setLogLevel(LogLevel.NOTHING);

        if(CONFIG.getApiDomain().equals("https://api.quickblox.com")){
            QBSettings.getInstance().setAccountKey(CONFIG.getAccountKey());
        }else{
            // specify custom domains
            QBSettings.getInstance().setEndpoints(CONFIG.getApiDomain(), CONFIG.getChatDomain(), ServiceZone.PRODUCTION);
            QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
        }
    }
}
