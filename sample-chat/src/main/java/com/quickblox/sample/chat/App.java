package com.quickblox.sample.chat;

import com.quickblox.core.QBSettings;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.sample.core.CoreApp;

public class App extends CoreApp {
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        QBSettings.getInstance().init(this, Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(Consts.QB_ACCOUNT_KEY);
    }
}
