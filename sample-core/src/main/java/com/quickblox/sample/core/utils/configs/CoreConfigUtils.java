package com.quickblox.sample.core.utils.configs;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.quickblox.sample.core.models.QbConfigs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CoreConfigUtils {

    public static QbConfigs getCoreConfigs(String fileName) throws IOException {
        ConfigParser configParser = new ConfigParser();
        Gson gson = new Gson();
        return gson.fromJson(configParser.getConfigsAsJsonString(fileName), QbConfigs.class);
    }

    public static String getStringConfigFromFile(String fileName, String fieldName) throws IOException, JSONException {
        JSONObject appConfigs = new ConfigParser().getConfigsAsJson(fileName);
        return appConfigs.getString(fieldName);
    }

    public static String getStringConfigFromFileOrNull(String fileName, String fieldName) {
        String fieldValue = null;

        try {
            fieldValue = getStringConfigFromFile(fileName, fieldName);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return fieldValue;
    }

    public static boolean isStringConfigFromFileNotEmpty(String fileName, String fieldName){
        return !TextUtils.isEmpty(getStringConfigFromFileOrNull(fileName, fieldName));
    }
}
