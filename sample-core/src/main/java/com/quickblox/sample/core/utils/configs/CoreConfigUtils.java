package com.quickblox.sample.core.utils.configs;

import com.google.gson.Gson;
import com.quickblox.sample.core.models.CoreConfigs;

/**
 * Created by tereha on 02.12.16.
 */

public class CoreConfigUtils {

    public static CoreConfigs getCoreConfigs() {
        CoreConfigParser coreConfigParser = CoreConfigParser.getInstance();
        Gson gson = new Gson();
        return gson.fromJson(coreConfigParser.getCoreConfigsAsJsonString(), CoreConfigs.class);
    }
}
