package com.quickblox.sample.chat.utils.configs;

import android.content.Context;

import com.quickblox.sample.chat.App;
import com.quickblox.sample.core.utils.AssetsUtils;

import java.io.IOException;

public class ConfigParser {


    private static ConfigParser configParser;
    private static String configsAsJsonString;

    private ConfigParser() {
    }

    public static ConfigParser getInstance() {
        if (configParser == null) {
            initConfigsFromFile();
            configParser = new ConfigParser();
        }

        return configParser;
    }

    private static void initConfigsFromFile() {
        Context context = App.getInstance().getApplicationContext();

        try {
            configsAsJsonString = AssetsUtils.getJsonAsString("app_config.json", context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getConfigsAsJsonString(){
        return this.configsAsJsonString;
    }
}
