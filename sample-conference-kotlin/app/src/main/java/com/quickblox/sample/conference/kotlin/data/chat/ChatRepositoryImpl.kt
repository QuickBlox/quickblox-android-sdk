package com.quickblox.sample.conference.kotlin.data.chat

import android.os.Bundle
import com.quickblox.auth.session.Query
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.request.QBMessageGetBuilder
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import com.quickblox.sample.conference.kotlin.domain.repositories.chat.ChatRepository
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class ChatRepositoryImpl : ChatRepository {
    @Throws(Exception::class)
    override fun loadHistorySync(dialog: QBChatDialog, messageGetBuilder: QBMessageGetBuilder): Pair<ArrayList<QBChatMessage>, Bundle> {
        val performer = QBRestChatService.getDialogMessages(dialog, messageGetBuilder) as Query
        return Pair(performer.perform(), performer.bundle)
    }

    override fun loadHistoryAsync(dialog: QBChatDialog, messageGetBuilder: QBMessageGetBuilder, callback: DataCallBack<ArrayList<QBChatMessage>, Exception>) {
        QBRestChatService.getDialogMessages(dialog, messageGetBuilder).performAsync(object : QBEntityCallback<ArrayList<QBChatMessage>> {
            override fun onSuccess(qbChatMessages: ArrayList<QBChatMessage>, bundle: Bundle) {
                callback.onSuccess(qbChatMessages, bundle)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }
}