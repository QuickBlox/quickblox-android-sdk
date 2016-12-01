package com.quickblox.sample.chat.utils;

import com.quickblox.sample.core.utils.CoreConfigParser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tereha on 01.12.16.
 */

public class ConfigParser extends CoreConfigParser {


    private static ConfigParser configParser;

    private ConfigParser() {
        super();
    }

    public static ConfigParser getInstance() {
        if (configParser == null) {
            configParser = new ConfigParser();
        }

        return configParser;
    }

    public JSONObject getUsersCongigs() {
        JSONObject usersConfigs = null;
        try {
            usersConfigs = new JSONObject(String.valueOf(allConfigs.getJSONObject("users_configs")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return usersConfigs;
    }

    public JSONObject getChatConfigs() {
        JSONObject chatConfigs = null;
        try {
            chatConfigs = new JSONObject(String.valueOf(allConfigs.getJSONObject("chat_connection_configs")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chatConfigs;
    }

    public int getChatPort() {
        return Integer.parseInt(getConfigByName(getChatConfigs(), "port"));
    }

    public String getChatHost() {
        return getConfigByName(getChatConfigs(), "host");
    }

    public String getChatServiceName() {
        return getConfigByName(getChatConfigs(), "service_name");
    }

    public int getChatSocketTimeout() {
        return Integer.parseInt(getConfigByName(getChatConfigs(), "socket_timeout"));
    }

    public boolean isKeepAlive() {
        return Boolean.parseBoolean(getConfigByName(getChatConfigs(), "keep_alive"));
    }


//
//
//
//
//    "sample_configs":{
//        "users_tag": "webrtcusers",
//                "users_password": "x6Bt0VDy5"
//    },
//


//                "auto_join": true,
//                "use_tls": true,
//                "use_stream_management": true,
//                "use_stream_management_resumption": true,
//                "auto_mark_delivered": true,
//                "preferred_resumption_time": 0,
//                "reconnection_allowed": true,
//                "allow_listen_network": true
//    },
//
//            "gcm_sender_id": "761750217637"

}
