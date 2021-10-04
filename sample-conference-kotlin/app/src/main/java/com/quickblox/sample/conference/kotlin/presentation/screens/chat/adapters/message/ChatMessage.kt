package com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message

import com.quickblox.chat.model.QBChatMessage
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ChatViewModel.Companion.MessageType.Companion.MESSAGE

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class ChatMessage constructor(val qbChatMessage: QBChatMessage, val type: Int = MESSAGE)