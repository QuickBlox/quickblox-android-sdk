package com.quickblox.sample.groupchatwebrtc.services.gcm;

import com.quickblox.sample.core.gcm.CoreGcmPushInstanceIDService;
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;

public class GcmPushInstanceIDService extends CoreGcmPushInstanceIDService {
    @Override
    protected String getSenderId() {
        return CoreConfigUtils.getCoreConfigs().getGcmSenderId();
    }
}
