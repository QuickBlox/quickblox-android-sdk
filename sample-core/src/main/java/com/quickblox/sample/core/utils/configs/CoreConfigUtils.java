package com.quickblox.sample.core.utils.configs;

import com.google.gson.Gson;
import com.quickblox.sample.core.models.CoreAppConfigs;

public class CoreConfigUtils {

    public static CoreAppConfigs getCoreConfigs() {
        CoreConfigParser coreConfigParser = CoreConfigParser.getInstance();
        Gson gson = new Gson();
        return gson.fromJson(coreConfigParser.getCoreConfigsAsJsonString(), CoreAppConfigs.class);
    }
}
