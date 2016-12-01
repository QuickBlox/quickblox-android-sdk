package com.quickblox.sample.core.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.quickblox.sample.core.CoreApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tereha on 01.12.16.
 */

public class CoreConfigParser {

    protected static JSONObject allConfigs;

    protected CoreConfigParser() {
        initConfigsFromFile();
    }

    private static void initConfigsFromFile() {
        Context context = CoreApp.getInstance().getApplicationContext();

        try {
            String jsonLocation = getJsonAsString("config.json", context);
            allConfigs = new JSONObject(jsonLocation);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private static String getJsonAsString(String filename, Context context) throws IOException {
        AssetManager manager = context.getAssets();
        InputStream file = manager.open(filename);
        byte[] formArray = new byte[file.available()];
        file.read(formArray);
        file.close();

        return new String(formArray);
    }

    public JSONObject getQbEndpoints() {
        JSONObject qbEndpoints = null;
        try {
            qbEndpoints = new JSONObject(String.valueOf(allConfigs.getJSONObject("qb_endpoints")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return qbEndpoints;
    }

    public String getAppId() {
        String appId = null;
        try {
            appId = getQbEndpoints().getString("app_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return appId;
    }

    public String getAuthKey() {
        String authKey = null;
        try {
            authKey = getQbEndpoints().getString("auth_key");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return authKey;
    }

    public String getAuthSecret() {
        String authSecret = null;
        try {
            authSecret = getQbEndpoints().getString("auth_secret");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return authSecret;
    }

    public String getAccountKey() {
        String accountKey = null;
        try {
            accountKey = getQbEndpoints().getString("account_key");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return accountKey;
    }

    public String getApiDomain() {
        String apiDomain = null;
        try {
            apiDomain = getQbEndpoints().getString("api_domain");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return apiDomain;
    }

    public String getChatDomain() {
        String chatDomain = null;
        try {
            chatDomain = getQbEndpoints().getString("chat_domain");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return chatDomain;
    }

    public String getGcmSenderId(){
        String gcmSenderId = null;
        try {
            gcmSenderId = allConfigs.getString("gcm_sender_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return gcmSenderId;
    }

    public String getConfigByName(JSONObject allConfigs, String configName){
        String value = null;
        try {
            value = allConfigs.getString(configName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return value;
    }
}
