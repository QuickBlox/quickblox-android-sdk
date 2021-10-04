package com.quickblox.sample.conference.kotlin.data.push

import com.quickblox.messages.services.fcm.QBFcmPushListenerService
import com.quickblox.sample.conference.kotlin.domain.repositories.notification.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class FcmPushListenerServiceImpl : QBFcmPushListenerService() {
    @Inject
    lateinit var notificationRepository: NotificationRepository

    override fun sendPushMessage(data: Map<*, *>, from: String, message: String) {
        super.sendPushMessage(data, from, message)
        notificationRepository.showNotification(message)
    }
}