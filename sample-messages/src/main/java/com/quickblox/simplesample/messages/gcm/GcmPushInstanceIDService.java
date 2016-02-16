package com.quickblox.simplesample.messages.gcm;

import com.quickblox.sample.core.gcm.CoreGcmPushInstanceIDService;
import com.quickblox.simplesample.messages.Consts;

public class GcmPushInstanceIDService extends CoreGcmPushInstanceIDService {
    @Override
    protected String getSenderId() {
        return Consts.GCM_SENDER_ID;
    }
}
