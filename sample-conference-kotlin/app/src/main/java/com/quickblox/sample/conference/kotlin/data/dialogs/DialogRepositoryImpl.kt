package com.quickblox.sample.conference.kotlin.data.dialogs

import android.os.Bundle
import com.quickblox.auth.session.Query
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import com.quickblox.chat.request.QBDialogRequestBuilder
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import com.quickblox.sample.conference.kotlin.domain.repositories.dialog.DialogRepository
import org.jivesoftware.smackx.muc.DiscussionHistory

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class DialogRepositoryImpl : DialogRepository {
    @Throws(Exception::class)
    override fun loadSync(requestBuilder: QBRequestGetBuilder): Pair<ArrayList<QBChatDialog>, Bundle> {
        val performer = QBRestChatService.getChatDialogs(QBDialogType.GROUP, requestBuilder) as Query
        return Pair(performer.perform(), performer.bundle)
    }

    override fun loadAsync(requestBuilder: QBRequestGetBuilder, callback: DataCallBack<ArrayList<QBChatDialog>, Exception>) {
        QBRestChatService.getChatDialogs(QBDialogType.GROUP, requestBuilder).performAsync(object : QBEntityCallback<ArrayList<QBChatDialog>> {
            override fun onSuccess(qbChatDialogs: ArrayList<QBChatDialog>, bundle: Bundle) {
                callback.onSuccess(qbChatDialogs, bundle)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }

    @Throws(Exception::class)
    override fun updateSync(dialog: QBChatDialog, requestBuilder: QBDialogRequestBuilder): Pair<QBChatDialog?, Bundle> {
        val performer = QBRestChatService.updateChatDialog(dialog, requestBuilder) as Query
        return Pair(performer.perform(), performer.bundle)
    }

    override fun updateAsync(dialog: QBChatDialog, requestBuilder: QBDialogRequestBuilder, callback: DataCallBack<QBChatDialog?, Exception>) {
        QBRestChatService.updateChatDialog(dialog, requestBuilder).performAsync(object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(dialog: QBChatDialog?, bundle: Bundle?) {
                callback.onSuccess(dialog, bundle)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }

    @Throws(Exception::class)
    override fun joinSync(chatDialog: QBChatDialog) {
        val history = DiscussionHistory()
        history.maxStanzas = 0
        chatDialog.join(history)
    }

    override fun joinAsync(chatDialog: QBChatDialog, callback: DataCallBack<Unit?, Exception>) {
        val history = DiscussionHistory()
        history.maxStanzas = 0
        chatDialog.join(history, object : QBEntityCallback<Unit> {
            override fun onSuccess(result: Unit?, bundle: Bundle?) {
                callback.onSuccess(result, bundle)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }

    @Throws(Exception::class)
    override fun getByIdSync(dialogId: String): QBChatDialog {
        return QBRestChatService.getChatDialogById(dialogId).perform()
    }

    override fun getByIdAsync(dialogId: String, callback: DataCallBack<QBChatDialog, Exception>) {
        QBRestChatService.getChatDialogById(dialogId).performAsync(object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle) {
                callback.onSuccess(qbChatDialog, bundle)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }

    @Throws(Exception::class)
    override fun createSync(dialog: QBChatDialog): QBChatDialog {
        return QBRestChatService.createChatDialog(dialog).perform()
    }

    override fun createAsync(dialog: QBChatDialog, callback: DataCallBack<QBChatDialog, Exception>) {
        QBRestChatService.createChatDialog(dialog).performAsync(object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle) {
                callback.onSuccess(qbChatDialog, bundle)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }

    @Throws(Exception::class)
    override fun deleteDialogsSync(dialogsIds: StringifyArrayList<String>, forceDelete: Boolean, bundle: Bundle): Pair<ArrayList<String>, Bundle> {
        val performer = QBRestChatService.deleteDialogs(dialogsIds, forceDelete, bundle) as Query
        return Pair(performer.perform(), performer.bundle)
    }

    override fun deleteDialogsAsync(dialogsIds: StringifyArrayList<String>, forceDelete: Boolean, bundle: Bundle, callback: DataCallBack<ArrayList<String>, Exception>) {
        QBRestChatService.deleteDialogs(dialogsIds, forceDelete, bundle).performAsync(object : QBEntityCallback<ArrayList<String>> {
            override fun onSuccess(dialogsIds: ArrayList<String>, bundle: Bundle) {
                callback.onSuccess(dialogsIds, bundle)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }
}