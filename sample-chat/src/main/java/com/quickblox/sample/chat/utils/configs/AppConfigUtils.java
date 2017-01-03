package com.quickblox.sample.chat.utils.configs;

import com.google.gson.Gson;
import com.quickblox.sample.chat.models.SampleConfigs;
import com.quickblox.sample.core.utils.configs.ConfigParser;
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;

import java.io.IOException;

public class AppConfigUtils extends CoreConfigUtils {

    public static SampleConfigs getConfigs(String fileName) throws IOException {
        ConfigParser configParser = new ConfigParser();
        Gson gson = new Gson();
        return gson.fromJson(configParser.getConfigsAsJsonString(fileName), SampleConfigs.class);
    }
}
