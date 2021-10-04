package com.quickblox.sample.conference.kotlin.domain.push

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface PushManager {
    fun showNotification(message: String)
    fun isSubscribed(): Boolean
    fun unSubscribe(unsubscribeCallback: () -> Unit)
}