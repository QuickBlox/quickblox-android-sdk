package com.quickblox.sample.videochat.java.utils;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.sample.videochat.java.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PushNotificationSender {
    public static void sendPushMessage(ArrayList<Integer> recipients, String senderName, String newSessionID,
                                       String opponentsIDs, String opponentsNames, boolean isVideoCall) {
        String outMessage = String.format(String.valueOf(R.string.text_push_notification_message), senderName);

        long timeStamp = System.currentTimeMillis();

        // Send Push: create QuickBlox Push Notification Event
        QBEvent qbEvent = new QBEvent();
        qbEvent.setNotificationType(QBNotificationType.PUSH);
        qbEvent.setEnvironment(QBEnvironment.DEVELOPMENT);
        // Generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)

        JSONObject json = new JSONObject();
        try {
            json.put("message", outMessage);
            json.put("ios_voip", "1");
            json.put("VOIPCall", "1");
            json.put("sessionID", newSessionID);
            json.put("opponentsIDs", opponentsIDs);
            json.put("contactIdentifier", opponentsNames);
            json.put("conferenceType", isVideoCall ? "1" : "2");
            json.put("timestamp", Long.toString(timeStamp));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        qbEvent.setMessage(json.toString());

        StringifyArrayList<Integer> userIds = new StringifyArrayList<>(recipients);
        qbEvent.setUserIds(userIds);

        QBPushNotifications.createEvent(qbEvent).performAsync(null);
    }
}