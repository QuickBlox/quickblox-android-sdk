package com.quickblox.sample.user;

import com.quickblox.core.QBSettings;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.user.definitions.Consts;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();

        //TODO seems like we can move this lines to core app class, and just pass appId, authSecret and others
        // Initialize QuickBlox application with credentials
        QBSettings.getInstance().init(getApplicationContext(), Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(Consts.ACCOUNT_KEY);
    }
}
