package com.quickblox.simplesample.messages;

import com.quickblox.core.QBSettings;
import com.quickblox.sample.core.CoreApp;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();

        QBSettings.getInstance().fastConfigInit(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET);
    }
}