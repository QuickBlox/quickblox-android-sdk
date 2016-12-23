package com.quickblox.sample.core.utils.configs;

import android.content.Context;

import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.utils.AssetsUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AppConfigParser {

    private Context context;

    public AppConfigParser() {
        context = CoreApp.getInstance().getApplicationContext();
    }

    public String getAppConfigsAsString(String fileName) throws IOException {
        return AssetsUtils.getJsonAsString(fileName, context);
    }

    public JSONObject getAppConfigsAsJson(String fileName) throws IOException, JSONException {
        return new JSONObject(getAppConfigsAsString(fileName));
    }

    public String getConfigByName(JSONObject jsonObject, String fieldName) throws JSONException {
        return jsonObject.getString(fieldName);
    }
}
