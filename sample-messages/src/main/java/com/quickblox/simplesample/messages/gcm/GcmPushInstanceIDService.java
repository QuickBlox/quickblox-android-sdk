package com.quickblox.simplesample.messages.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.quickblox.simplesample.messages.Consts;

public class GcmPushInstanceIDService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        GooglePlayServicesHelper playServicesHelper = new GooglePlayServicesHelper();
        if (playServicesHelper.checkPlayServicesAvailable()) {
            playServicesHelper.registerForGcm(Consts.GCM_SENDER_ID);
        }
    }
}
