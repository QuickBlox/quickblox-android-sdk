package com.quickblox.sample.groupchatwebrtc;

import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.groupchatwebrtc.definitions.Consts;
import com.quickblox.sample.groupchatwebrtc.util.QBRestUtils;

public class App extends CoreApp {
    QBRestUtils qbRestUtils;

    @Override
    public void onCreate() {
        super.onCreate();

        super.initCredentials(Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET, Consts.ACCOUNT_KEY);
        initQbRestUtils();
    }

    private void initQbRestUtils(){
        this.qbRestUtils = new QBRestUtils();
    }
}
