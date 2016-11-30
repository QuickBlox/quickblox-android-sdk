package com.quickblox.sample.user;

import com.quickblox.auth.session.QBSession;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.auth.session.QBSessionParameters;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.user.utils.Consts;

public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();
        initCredentials(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET, Consts.QB_ACCOUNT_KEY);
        QBSessionManager.getInstance().addListener(new QBSessionManager.QBSessionListener() {
            @Override
            public void onSessionCreated(QBSession qbSession) {
                Toaster.shortToast("Session Created");
            }

            @Override
            public void onSessionUpdated(QBSessionParameters qbSessionParameters) {
                Toaster.shortToast("Session Updated");
            }

            @Override
            public void onSessionDeleted() {
                Toaster.shortToast("Session Deleted");
            }

            @Override
            public void onSessionRestored(QBSession qbSession) {
                Toaster.shortToast("Session Restored");
            }

            @Override
            public void onSessionExpired() {
                Toaster.shortToast("Session Expired");
            }
        });
    }
}
