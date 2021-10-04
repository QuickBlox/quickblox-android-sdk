package com.quickblox.sample.conference.kotlin.domain.push

import com.quickblox.sample.conference.kotlin.domain.repositories.notification.NotificationRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.push.PushRepository

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class PushManagerImpl(private val pushRepository: PushRepository, private val notificationRepository: NotificationRepository) : PushManager {
    override fun showNotification(message: String) {
        notificationRepository.showNotification(message)
    }

    override fun unSubscribe(unsubscribeCallback: () -> Unit) {
        pushRepository.unsubscribe(unsubscribeCallback)
    }

    override fun isSubscribed(): Boolean {
        return pushRepository.isSubscribed()
    }
}