package com.quickblox.sample.conference.kotlin.domain.repositories.connection

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface ConnectivityChangedListener {
    fun onAvailable()
    fun onLost()
}