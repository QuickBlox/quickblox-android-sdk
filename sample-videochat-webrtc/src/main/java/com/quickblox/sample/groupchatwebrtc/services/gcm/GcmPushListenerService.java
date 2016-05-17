package com.quickblox.sample.groupchatwebrtc.services.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.quickblox.sample.core.gcm.CoreGcmPushListenerService;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.core.utils.constant.GcmConsts;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.services.LoginToChatAndCallListenerService;
import com.quickblox.users.model.QBUser;

/**
 * Created by tereha on 13.05.16.
 */
public class GcmPushListenerService extends GcmListenerService {
    private static final String TAG = CoreGcmPushListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(GcmConsts.EXTRA_GCM_MESSAGE);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);

        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        if (sharedPrefsHelper.hasQbUser()) {
            Log.d(TAG, "App have logined user");
            QBUser qbUser = sharedPrefsHelper.getQbUser();
            startLogineService(qbUser);
        }
    }

    private void startLogineService(QBUser qbUser){
        Intent intent;

        intent = new Intent(this, LoginToChatAndCallListenerService.class);
        intent.putExtra(Consts.EXTRA_USER_LOGIN, qbUser.getLogin());
        intent.putExtra(Consts.EXTRA_USER_PASSWORD, Consts.DEFAULT_USER_PASSWORD);
        intent.putExtra(Consts.EXTRA_USER_ID, qbUser.getId());
        startService(intent);
    }
}