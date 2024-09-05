package com.quickblox.sample.conference.kotlin.domain.chat

import androidx.collection.ArraySet
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.attachment.AttachmentModel
import com.quickblox.users.model.QBUser

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface ChatManager {
    fun loginToChat(user: QBUser?, callback: DomainCallback<Unit, Exception>)
    fun loadDialogs(refresh: Boolean, reJoin: Boolean, callback: DomainCallback<ArrayList<QBChatDialog>, Exception>)
    fun deleteDialogs(dialogsDelete: ArrayList<QBChatDialog>, qbUser: QBUser, callback: DomainCallback<List<QBChatDialog>, Exception>)
    fun destroyChat()
    fun isLoggedInChat(): Boolean
    fun joinDialogs(dialogs: List<QBChatDialog>)
    fun joinDialog(dialog: QBChatDialog, callback: DataCallBack<Unit?, Exception>)
    fun subscribeChatListener(chatListener: ChatListener)
    fun unsubscribeChatListener(chatListener: ChatListener)
    fun subscribeDialogListener(dialogListener: DialogListener)
    fun unsubscribeDialogListener(dialogListener: DialogListener)
    fun subscribeConnectionChatListener(connectionChatListener: ConnectionChatListener)
    fun unSubscribeConnectionChatListener(connectionChatListener: ConnectionChatListener)
    fun loadDialogById(dialogId: String, callback: DomainCallback<QBChatDialog, Exception>)
    fun createDialog(users: List<QBUser>, chatName: String, callback: DomainCallback<QBChatDialog, Exception>)
    fun loadMessages(dialog: QBChatDialog, skipPagination: Int, callback: DomainCallback<ArrayList<QBChatMessage>, Exception>)
    fun leaveDialog(dialog: QBChatDialog, qbUser: QBUser, callback: DomainCallback<QBChatDialog, Exception>)
    fun readMessage(qbChatMessage: QBChatMessage, qbDialog: QBChatDialog, callback: DomainCallback<Unit, Exception>)
    fun buildAndSendMessage(currentUser: QBUser, dialog: QBChatDialog, text: String, attachmentModels: ArrayList<AttachmentModel>, callback: DomainCallback<Unit, Exception>)
    fun sendCreateConference(dialog: QBChatDialog, callback: DomainCallback<Unit, Exception>)
    fun getDialogs(): ArrayList<QBChatDialog>
    fun clearDialogs()
    fun addUsersToDialog(dialog: QBChatDialog, users: ArraySet<QBUser>, callback: DomainCallback<QBChatDialog?, Exception>)
    fun buildAndSendStartStreamMessage(dialog: QBChatDialog, streamId: String, callback: DomainCallback<Unit, Exception>)
}