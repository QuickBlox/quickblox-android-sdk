package com.quickblox.sample.conference.kotlin.domain.chat

import com.quickblox.chat.model.QBChatDialog

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface DialogListener {
    fun onUpdatedDialog(dialog: QBChatDialog)
    fun onError(exception: Exception)
}