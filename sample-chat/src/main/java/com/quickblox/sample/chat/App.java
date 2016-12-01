package com.quickblox.sample.chat;

import android.text.TextUtils;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.ServiceZone;
import com.quickblox.sample.chat.utils.ConfigParser;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.utils.ActivityLifecycle;

public class App extends CoreApp {
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityLifecycle.init(this);
        ConfigParser configParser = ConfigParser.getInstance();
        initCredentials(configParser.getAppId(), configParser.getAuthKey(), configParser.getAuthSecret(), configParser.getAccountKey());

        if (!TextUtils.isEmpty(configParser.getApiDomain()) && !TextUtils.isEmpty(configParser.getChatDomain())){
            QBSettings.getInstance().setEndpoints(configParser.getApiDomain(), configParser.getChatDomain(), ServiceZone.PRODUCTION);
            QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
        }
    }
}