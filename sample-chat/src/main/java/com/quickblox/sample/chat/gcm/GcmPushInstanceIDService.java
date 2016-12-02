package com.quickblox.sample.chat.gcm;

import com.quickblox.sample.chat.utils.configs.ConfigUtils;
import com.quickblox.sample.core.gcm.CoreGcmPushInstanceIDService;

public class GcmPushInstanceIDService extends CoreGcmPushInstanceIDService {
    @Override
    protected String getSenderId() {
        return ConfigUtils.getCoreConfigs().getGcmSenderId();
    }
}
