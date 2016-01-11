package com.quickblox.sample.chat;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.LogLevel;
import com.quickblox.core.QBSettings;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.sample.core.CoreApp;

public class App extends CoreApp {
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        QBSettings.getInstance().fastConfigInit(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET);

        if (BuildConfig.DEBUG) {
            QBChatService.setDebugEnabled(true);
            QBSettings.getInstance().setLogLevel(LogLevel.DEBUG);
        } else {
            QBChatService.setDebugEnabled(false);
            QBSettings.getInstance().setLogLevel(LogLevel.NOTHING);
        }
    }
}
