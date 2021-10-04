package com.quickblox.sample.conference.kotlin.domain.repositories.push

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface PushRepository {
    fun isSubscribed(): Boolean
    fun unsubscribe(unsubscribeCallback: () -> Unit)
}