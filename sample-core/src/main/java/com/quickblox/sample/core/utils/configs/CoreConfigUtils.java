package com.quickblox.sample.core.utils.configs;

import com.google.gson.Gson;
import com.quickblox.sample.core.models.QbAppConfigs;

import java.io.IOException;

public class CoreConfigUtils {

    public static QbAppConfigs getCoreConfigs(String fileName) throws IOException {
        AppConfigParser appConfigParser = new AppConfigParser ();
        Gson gson = new Gson();
        return gson.fromJson(appConfigParser.getAppConfigsAsString(fileName), QbAppConfigs.class);
    }
}
