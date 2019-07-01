package com.quickblox.sample.videochat.kotlin.utils

import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.messages.QBPushNotifications
import com.quickblox.messages.model.QBEnvironment
import com.quickblox.messages.model.QBEvent
import com.quickblox.messages.model.QBNotificationType
import com.quickblox.sample.videochat.kotlin.R
import java.util.*

fun sendPushMessage(recipients: ArrayList<Int>, senderName: String) {
    val outMessage = String.format(R.string.text_push_notification_message.toString(), senderName)

    // Send Push: create QuickBlox Push Notification Event
    val qbEvent = QBEvent()
    qbEvent.notificationType = QBNotificationType.PUSH
    qbEvent.environment = QBEnvironment.DEVELOPMENT
    // Generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)
    qbEvent.message = outMessage

    val userIds = StringifyArrayList(recipients)
    qbEvent.userIds = userIds

    QBPushNotifications.createEvent(qbEvent).performAsync(null)
}