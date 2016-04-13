package com.quickblox.sample.groupchatwebrtc;

import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.groupchatwebrtc.definitions.Consts;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();

        super.initCredentials(Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET, Consts.ACCOUNT_KEY);
    }
}
