package com.quickblox.chat_v2.apis;

import android.os.Bundle;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.module.users.model.QBUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;

public class FaceBookManager {

    private boolean isMeInfo;
    private QBUser user;

    // Get my info
    public void getUserInfo(boolean isMe, QBUser qbUser) throws MalformedURLException, IOException, JSONException {
        String fqlQuery;
        user = qbUser;
        isMeInfo = isMe;

        if (isMe) {
            fqlQuery = "SELECT pic FROM user WHERE uid =me()";
        } else {
            fqlQuery = "SELECT pic FROM user WHERE uid =" + qbUser.getFacebookId();
        }

        Bundle params = new Bundle();
        params.putString("q", fqlQuery);
        params.putString("access_token", ChatApplication.getInstance().getAccessTokien());
        Session session = Session.getActiveSession();
        Request request = new Request(session, "/fql", params, HttpMethod.GET, new Request.Callback() {

            public void onCompleted(Response response) {

                GraphObject graphObject = response.getGraphObject();

                JSONObject jsonObject = graphObject.getInnerJSONObject();
                JSONArray array;
                try {
                    array = jsonObject.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jObject = array.getJSONObject(i);

                        // insert in this field url pricture
                        String pic = jObject.getString("pic");
                        if (pic == null) {
                            return;
                        }
                        if (isMeInfo) {
                            ChatApplication.getInstance().getQbUser().setWebsite(pic);
                        } else {
                            user.setWebsite(pic);
                        }

                    }
                } catch (JSONException e) {

                    e.printStackTrace();
                }

            }

        });
        Request.executeBatchAsync(request);

    }
}
