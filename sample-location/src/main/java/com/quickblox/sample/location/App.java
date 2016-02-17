package com.quickblox.sample.location;

import com.quickblox.core.QBSettings;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.location.utils.Consts;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize QuickBlox application with credentials.
        QBSettings.getInstance().init(this, Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(Consts.ACCOUNT_KEY);
    }
}
