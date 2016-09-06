package com.quickblox.sample.groupchatwebrtc.services.gcm;

import com.quickblox.sample.core.gcm.CoreGcmPushInstanceIDService;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;

public class GcmPushInstanceIDService extends CoreGcmPushInstanceIDService {
    @Override
    protected String getSenderId() {
        return Consts.GCM_SENDER_ID;
    }
}
