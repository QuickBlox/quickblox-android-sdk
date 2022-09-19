package com.quickblox.sample.videochat.java.utils;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.sample.videochat.java.R;

import org.json.JSONException;
import org.json.JSONObject;

public class PushNotificationSender {
    public static void sendPushMessage(Integer recipientId, String senderName, String sessionId,
                                       String opponentIds, String opponentNames, boolean isVideoCall) {
        String outMessage = String.format(String.valueOf(R.string.text_push_notification_message), senderName);

        long timeStamp = System.currentTimeMillis();

        // send Push: create QuickBlox Push Notification Event
        QBEvent event = new QBEvent();
        event.setNotificationType(QBNotificationType.PUSH);
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        // generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)

        JSONObject json = new JSONObject();
        try {
            json.put("message", outMessage);
            json.put("ios_voip", "1");
            json.put("VOIPCall", "1");
            json.put("sessionID", sessionId);
            json.put("opponentsIDs", opponentIds);
            json.put("contactIdentifier", opponentNames);
            json.put("conferenceType", isVideoCall ? "1" : "2");
            json.put("timestamp", Long.toString(timeStamp));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        event.setMessage(json.toString());

        StringifyArrayList<Integer> userIds = new StringifyArrayList<>();
        userIds.add(recipientId);
        event.setUserIds(userIds);
        QBPushNotifications.createEvent(event).performAsync(null);
    }
}