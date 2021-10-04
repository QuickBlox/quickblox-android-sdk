package com.quickblox.sample.conference.kotlin.domain.repositories.dialog

import android.os.Bundle
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.request.QBDialogRequestBuilder
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface DialogRepository {
    fun loadSync(requestBuilder: QBRequestGetBuilder): Pair<ArrayList<QBChatDialog>, Bundle>
    fun loadAsync(requestBuilder: QBRequestGetBuilder, callback: DataCallBack<ArrayList<QBChatDialog>, Exception>)

    fun updateSync(dialog: QBChatDialog, requestBuilder: QBDialogRequestBuilder): Pair<QBChatDialog?, Bundle>
    fun updateAsync(dialog: QBChatDialog, requestBuilder: QBDialogRequestBuilder, callback: DataCallBack<QBChatDialog?, Exception>)

    fun joinSync(chatDialog: QBChatDialog)
    fun joinAsync(chatDialog: QBChatDialog, callback: DataCallBack<Unit?, Exception>)

    fun getByIdSync(dialogId: String): QBChatDialog
    fun getByIdAsync(dialogId: String, callback: DataCallBack<QBChatDialog, Exception>)

    fun createSync(dialog: QBChatDialog): QBChatDialog
    fun createAsync(dialog: QBChatDialog, callback: DataCallBack<QBChatDialog, Exception>)

    fun deleteDialogsSync(dialogsIds: StringifyArrayList<String>, forceDelete: Boolean, bundle: Bundle): Pair<ArrayList<String>, Bundle>
    fun deleteDialogsAsync(dialogsIds: StringifyArrayList<String>, forceDelete: Boolean, bundle: Bundle, callback: DataCallBack<ArrayList<String>, Exception>)
}