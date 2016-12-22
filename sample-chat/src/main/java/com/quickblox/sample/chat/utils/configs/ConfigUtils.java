package com.quickblox.sample.chat.utils.configs;

import com.google.gson.Gson;
import com.quickblox.sample.chat.models.AppConfigs;
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;

public class ConfigUtils extends CoreConfigUtils {

    public static AppConfigs getConfigs() {
        ConfigParser configParser = ConfigParser.getInstance();
        Gson gson = new Gson();
        return gson.fromJson(configParser.getConfigsAsJsonString(), AppConfigs.class);
    }
}
