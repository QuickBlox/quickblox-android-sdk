package com.quickblox.sample.customobjects;

import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.customobjects.definition.Consts;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();
        super.initCredentials(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET, Consts.QB_ACCOUNT_KEY);
    }
}
