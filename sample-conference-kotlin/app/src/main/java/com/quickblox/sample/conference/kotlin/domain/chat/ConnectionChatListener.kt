package com.quickblox.sample.conference.kotlin.domain.chat

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface ConnectionChatListener {
    fun onConnectedChat()
    fun onError(exception: Exception)
    fun reconnectionFailed(exception: Exception)
}