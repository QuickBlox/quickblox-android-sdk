package com.quickblox.sample.conference.kotlin.domain.repositories.chat

import android.os.Bundle
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.request.QBMessageGetBuilder
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface ChatRepository {
    fun loadHistorySync(dialog: QBChatDialog, messageGetBuilder: QBMessageGetBuilder): Pair<ArrayList<QBChatMessage>, Bundle>
    fun loadHistoryAsync(dialog: QBChatDialog, messageGetBuilder: QBMessageGetBuilder, callback: DataCallBack<ArrayList<QBChatMessage>, Exception>)
}