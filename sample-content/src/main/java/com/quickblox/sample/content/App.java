package com.quickblox.sample.content;

import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.core.CoreApp;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();
        initCredentials(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET, Consts.QB_ACCOUNT_KEY);
    }
}
