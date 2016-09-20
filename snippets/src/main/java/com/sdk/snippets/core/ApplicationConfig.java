package com.sdk.snippets.core;

import android.content.Context;
import android.util.Log;

import com.quickblox.core.helper.Lo;
import com.sdk.snippets.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by igorkhomenko on 7/9/14.
 */
public class ApplicationConfig {

    private static ApplicationConfig instance;
    private Context ctx;
    private Map<String, Map<String, Object>> servers = new HashMap<String, Map<String, Object>>();
    private String activeServer;
    private boolean useUser1;

    public static synchronized ApplicationConfig init(Context ctx){
        if (instance == null) {
            instance = new ApplicationConfig(ctx);
        }

        return instance;
    }

    public static synchronized ApplicationConfig getInstance() {
        if(instance == null){
            throw new NullPointerException("must call 'init' first");
        }
        return instance;
    }


    private ApplicationConfig(Context ctx) {
        this.ctx = ctx;

        parseJsonServers();

        Log.d("ApplicationConfig", servers.toString());
    }

    private void parseJsonServers(){
        //Get Data From Text Resource File Contains Json Data.
        InputStream inputStream = ctx.getResources().openRawResource(R.raw.servers);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // Parse the data into jsonobject to get original data in form of json.
            JSONObject jObject = new JSONObject(byteArrayOutputStream.toString());


            // pasre server
            JSONObject jObjectResult = jObject.getJSONObject("servers");
            Iterator<String> keys = jObjectResult.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                JSONObject val = jObjectResult.getJSONObject(key);

                Map<String, Object> serverConfig = new HashMap<String, Object>();

                Iterator<String> keysServer = val.keys();
                while(keysServer.hasNext()) {
                    String keyServer = keysServer.next();
                    Object valueServer = val.get(keyServer);

                    serverConfig.put(keyServer, valueServer);
                }

                servers.put(key, serverConfig);
            }

            //parse other data
            activeServer = jObject.getString("active");
            useUser1 = jObject.getBoolean("use_first_user");


//            // fast run mode (only for debug)
//            //
//            if(android.os.Build.MODEL.equals("Galaxy Nexus")){
//                useUser1 = false;
//            }else{
//                useUser1 = true;
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAppId(){
        return servers.get(activeServer).get("app_id").toString();
    }
    public String getAuthKey(){
        return (String)servers.get(activeServer).get("auth_key");
    }
    public String getAuthSecret(){
        return (String)servers.get(activeServer).get("auth_secret");
    }

    public String getAccountKey(){
        return (String)servers.get(activeServer).get("account_key");
    }
    //
    public String getApiDomain(){
        return (String)servers.get(activeServer).get("api_domain");
    }
    public String getChatDomain(){
        return (String)servers.get(activeServer).get("chat_domain");
    }
    public String getBucketName(){
        return (String)servers.get(activeServer).get("bucket_name");
    }
    //
    public Integer getTestUserId1(){
        if(useUser1) {
            return (Integer) servers.get(activeServer).get("test_user_id1");
        }else{
            return (Integer)servers.get(activeServer).get("test_user_id2");
        }
    }
    public String getTestUserLogin1(){
        if(useUser1) {
            return (String) servers.get(activeServer).get("test_user_login1");
        }else{
            return (String) servers.get(activeServer).get("test_user_login2");
        }
    }
    public String getTestUserPassword1(){
        if(useUser1) {
            return (String) servers.get(activeServer).get("test_user_password1");
        }else{
            return (String) servers.get(activeServer).get("test_user_password2");
        }
    }
    public Integer getTestUserId2(){
        if(useUser1) {
            return (Integer) servers.get(activeServer).get("test_user_id2");
        }else{
            return (Integer)servers.get(activeServer).get("test_user_id1");
        }
    }
    public String getTestUserLogin2(){
        if(useUser1) {
            return (String) servers.get(activeServer).get("test_user_login2");
        }else{
            return (String) servers.get(activeServer).get("test_user_login1");
        }
    }
    public String getTestUserPassword2(){
        if(useUser1) {
            return (String) servers.get(activeServer).get("test_user_password2");
        }else{
            return (String) servers.get(activeServer).get("test_user_password1");
        }
    }
    //
    public String getTestDialogId(){
        return (String) servers.get(activeServer).get("dialog_id");
    }
    public String getTestRoomJid(){
        return String.format("%s_%s@muc.%s", getAppId(), getTestDialogId(), getChatDomain());
    }
}
