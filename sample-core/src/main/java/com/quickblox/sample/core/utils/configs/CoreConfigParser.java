package com.quickblox.sample.core.utils.configs;

import android.content.Context;

import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.utils.AssetsUtils;

import java.io.IOException;

public class CoreConfigParser {


    private static String coreConfigsAsJsonString;
    private static CoreConfigParser coreConfigParser;

    private CoreConfigParser() {
    }

    public static CoreConfigParser getInstance(){
        if (coreConfigParser == null){
            initConfigsFromFile();
            coreConfigParser = new CoreConfigParser();
        }

        return coreConfigParser;
    }

    private static void initConfigsFromFile() {
        Context context = CoreApp.getInstance().getApplicationContext();

        try {
            coreConfigsAsJsonString = AssetsUtils.getJsonAsString("core_app_config.json", context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCoreConfigsAsJsonString(){
        return this.coreConfigsAsJsonString;
    }
}
