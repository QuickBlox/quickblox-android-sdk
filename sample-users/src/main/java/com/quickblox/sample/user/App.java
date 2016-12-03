package com.quickblox.sample.user;

import android.util.Log;

import com.quickblox.auth.session.QBSession;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.auth.session.QBSessionParameters;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.user.utils.Consts;

public class App extends CoreApp {
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        initCredentials(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET, Consts.QB_ACCOUNT_KEY);
        initQBSessionManager();
    }

    private void initQBSessionManager() {
        QBSessionManager.getInstance().addListener(new QBSessionManager.QBSessionListener() {
            @Override
            public void onSessionCreated(QBSession qbSession) {
                Log.d(TAG, "Session Created");
            }

            @Override
            public void onSessionUpdated(QBSessionParameters qbSessionParameters) {
                Log.d(TAG, "Session Updated");
            }

            @Override
            public void onSessionDeleted() {
                Log.d(TAG, "Session Deleted");
            }

            @Override
            public void onSessionRestored(QBSession qbSession) {
                Log.d(TAG, "Session Restored");
            }

            @Override
            public void onSessionExpired() {
                Log.d(TAG, "Session Expired");
            }
        });
    }
}
