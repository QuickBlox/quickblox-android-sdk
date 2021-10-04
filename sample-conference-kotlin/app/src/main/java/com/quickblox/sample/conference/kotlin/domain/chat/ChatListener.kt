package com.quickblox.sample.conference.kotlin.domain.chat

import com.quickblox.chat.model.QBChatMessage

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface ChatListener {
    fun onReceivedMessage(dialogId: String, message: QBChatMessage, updatedDialog: Boolean)
    fun onError(exception: Exception)
}