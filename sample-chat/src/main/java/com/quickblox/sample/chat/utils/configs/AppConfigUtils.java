package com.quickblox.sample.chat.utils.configs;

import com.google.gson.Gson;
import com.quickblox.sample.chat.models.AppConfigs;
import com.quickblox.sample.core.utils.configs.AppConfigParser;
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;

import java.io.IOException;

public class AppConfigUtils extends CoreConfigUtils {

    public static AppConfigs getConfigs(String fileName) throws IOException {
        AppConfigParser appConfigParser = new AppConfigParser ();
        Gson gson = new Gson();
        return gson.fromJson(appConfigParser.getAppConfigsAsString(fileName), AppConfigs.class);
    }
}
