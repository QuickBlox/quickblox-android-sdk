package com.quickblox.sample.pushnotifications;

import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;
import com.quickblox.messages.services.QBPushManager;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.utils.ActivityLifecycle;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.pushnotifications.utils.Consts;

public class App extends CoreApp {

    private static final String TAG = App.class.getSimpleName();
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ActivityLifecycle.init(this);

        initPushManager();
    }

    private void initPushManager() {
        QBPushManager.getInstance().addListener(new QBPushManager.QBSubscribeListener() {
            @Override
            public void onSubscriptionCreated() {
                Toaster.shortToast("Subscription Created");
                Log.d(TAG, "SubscriptionCreated");
            }

            @Override
            public void onSubscriptionError(Exception e, int resultCode) {
                Log.d(TAG, "SubscriptionError" + e.getLocalizedMessage());
                if (resultCode >= 0) {
                    String error = GoogleApiAvailability.getInstance().getErrorString(resultCode);
                    Log.d(TAG, "SubscriptionError playServicesAbility: " + error);
                }
                Toaster.shortToast(e.getLocalizedMessage());
            }

            @Override
            public void onSubscriptionDeleted(boolean success) {

            }
        });
    }

    public static synchronized App getInstance() {
        return instance;
    }
}