package com.quickblox.sample.pushnotifications;

import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;
import com.quickblox.messages.services.QBPushManager;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.utils.ActivityLifecycle;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.core.utils.Toaster;

public class App extends CoreApp {

    private static final String TAG = App.class.getSimpleName();
    private static App instance;

    private static final String FCM_CHANNEL_KEY = "fcm_channel";
    private static final String GCM_CHANNEL_KEY = "gcm_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ActivityLifecycle.init(this);
        setEnabledPushChannels();
        initPushManager();
    }

    private void setEnabledPushChannels() {
        SharedPrefsHelper.getInstance().save(GCM_CHANNEL_KEY, true);
        SharedPrefsHelper.getInstance().save(FCM_CHANNEL_KEY, true);
    }

    public Boolean isGcmEnabled() {
        return SharedPrefsHelper.getInstance().get(GCM_CHANNEL_KEY, true);
    }

    public Boolean isFcmEnabled() {
        return SharedPrefsHelper.getInstance().get(FCM_CHANNEL_KEY, true);
    }

    public void setEnableFcmChannel(Boolean enable) {
        SharedPrefsHelper.getInstance().save(FCM_CHANNEL_KEY, enable);
    }

    public void setEnableGcmChannel(Boolean enable) {
        SharedPrefsHelper.getInstance().save(GCM_CHANNEL_KEY, enable);
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