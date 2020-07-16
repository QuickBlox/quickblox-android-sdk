package com.quickblox.sample.videochat.kotlin.utils

import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.messages.QBPushNotifications
import com.quickblox.messages.model.QBEnvironment
import com.quickblox.messages.model.QBEvent
import com.quickblox.messages.model.QBNotificationType
import com.quickblox.sample.videochat.kotlin.R
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

fun sendPushMessage(recipients: ArrayList<Int>,
                    senderName: String,
                    newSessionID: String,
                    opponentsIDs: String,
                    opponentsNames: String,
                    isVideoCall: Boolean) {
    val outMessage = String.format(R.string.text_push_notification_message.toString(), senderName)

    val currentTime = Calendar.getInstance().time
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val eventDate = simpleDateFormat.format(currentTime)

    // Send Push: create QuickBlox Push Notification Event
    val qbEvent = QBEvent()
    qbEvent.notificationType = QBNotificationType.PUSH
    qbEvent.environment = QBEnvironment.DEVELOPMENT
    // Generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)

    val json = JSONObject()
    try {
        json.put("message", outMessage)
        json.put("ios_voip", "1")
        json.put("VOIPCall", "1")
        json.put("sessionID", newSessionID)
        json.put("opponentsIDs", opponentsIDs)
        json.put("contactIdentifier", opponentsNames)
        json.put("conferenceType", if (isVideoCall) "1" else "2")
        json.put("timestamp", eventDate)
    } catch (e: JSONException) {
        e.printStackTrace()
    }

    qbEvent.message = json.toString()

    val userIds = StringifyArrayList(recipients)
    qbEvent.userIds = userIds

    QBPushNotifications.createEvent(qbEvent).performAsync(null)
}