package com.quickblox.sample.conference.kotlin.domain.repositories.notification

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface NotificationRepository {
    fun showNotification(message: String)
}