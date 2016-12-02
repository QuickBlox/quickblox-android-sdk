package com.quickblox.sample.chat.utils.configs;

import com.google.gson.Gson;
import com.quickblox.sample.chat.models.Configs;
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;

/**
 * Created by tereha on 02.12.16.
 */

public class ConfigUtils extends CoreConfigUtils {

    public static Configs getConfigs() {
        ConfigParser configParser = ConfigParser.getInstance();
        Gson gson = new Gson();
        return gson.fromJson(configParser.getConfigsAsJsonString(), Configs.class);
    }
}
