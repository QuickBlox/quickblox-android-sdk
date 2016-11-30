package com.quickblox.sample.core.gcm;

import android.util.Log;

import com.quickblox.messages.services.gcm.QBGcmPushInstanceIDService;

public abstract class CoreGcmPushInstanceIDService extends QBGcmPushInstanceIDService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Log.d(getClass().getSimpleName(), "onTokenRefresh");
    }

    protected abstract String getSenderId();
}
