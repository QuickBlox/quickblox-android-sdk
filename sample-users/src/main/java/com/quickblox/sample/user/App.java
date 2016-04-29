package com.quickblox.sample.user;

import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.user.utils.Consts;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();
        initCredentials(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET, Consts.QB_ACCOUNT_KEY);
    }
}
