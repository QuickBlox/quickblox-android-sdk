package com.quickblox.sample.content;

import com.quickblox.core.QBSettings;
import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.core.CoreApp;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();

        QBSettings.getInstance().init(this, Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(Consts.ACCOUNT_KEY);
    }
}
