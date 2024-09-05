package com.quickblox.sample.conference.kotlin.domain.chat

import android.os.Bundle
import android.text.TextUtils
import androidx.collection.ArraySet
import com.quickblox.chat.QBChatService
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.QBAttachment
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.chat.request.QBDialogRequestBuilder
import com.quickblox.chat.request.QBMessageGetBuilder
import com.quickblox.content.model.QBFile
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.repositories.chat.ChatRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.db.DBRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.dialog.DialogRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.settings.SettingsRepository
import com.quickblox.sample.conference.kotlin.domain.user.USER_DEFAULT_PASSWORD
import com.quickblox.sample.conference.kotlin.executor.Executor
import com.quickblox.sample.conference.kotlin.executor.ExecutorTask
import com.quickblox.sample.conference.kotlin.executor.TaskExistException
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManager
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.attachment.AttachmentModel
import com.quickblox.sample.conference.kotlin.presentation.screens.main.DIALOGS_LIMIT
import com.quickblox.sample.conference.kotlin.presentation.screens.main.EXTRA_SKIP
import com.quickblox.sample.conference.kotlin.presentation.screens.main.EXTRA_TOTAL_ENTRIES
import com.quickblox.users.model.QBUser
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smackx.muc.DiscussionHistory

private const val PROPERTY_OCCUPANTS_IDS = "current_occupant_ids"
private const val PROPERTY_DIALOG_TYPE = "type"
private const val PROPERTY_DIALOG_NAME = "room_name"
private const val PROPERTY_NOTIFICATION_TYPE = "notification_type"
private const val PROPERTY_NEW_OCCUPANTS_IDS = "new_occupants_ids"
const val PROPERTY_CONVERSATION_ID = "conference_id"
private const val CREATING_DIALOG = "1"
private const val OCCUPANTS_ADDED = "2"
private const val OCCUPANT_LEFT = "3"
private const val START_CONFERENCE = "4"
private const val START_STREAM = "5"
private const val LOAD_DIALOGS = "load_dialogs"

const val CHAT_HISTORY_ITEMS_PER_PAGE = 50
private const val CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent"

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class ChatManagerImpl(private val dbRepository: DBRepository, private val dialogRepository: DialogRepository,
                      private val chatRepository: ChatRepository, private val resourcesManager: ResourcesManager,
                      private val executor: Executor, private val settingsRepository: SettingsRepository) : ChatManager {
    private var chatListeners = hashSetOf<ChatListener>()
    private var dialogListeners = hashSetOf<DialogListener>()

    private var systemMessagesListener = SystemMessagesListener()
    private var dialogsMessageListener = DialogsMessageListener()
    private var chatMessageListener = ChatMessageListener()
    private var connectionListener: ChatConnectionListener? = null
    private var chatService: QBChatService? = QBChatService.getInstance()
    private val dialogs = arrayListOf<QBChatDialog>()
    private var totalCount = 0
    private var skip = 0
    private var connectionChatListeners = hashSetOf<ConnectionChatListener>()

    override fun subscribeConnectionChatListener(connectionChatListener: ConnectionChatListener) {
        connectionChatListeners.add(connectionChatListener)
    }

    override fun unSubscribeConnectionChatListener(connectionChatListener: ConnectionChatListener) {
        connectionChatListeners.remove(connectionChatListener)
    }

    override fun subscribeDialogListener(dialogListener: DialogListener) {
        chatService?.systemMessagesManager?.addSystemMessageListener(systemMessagesListener)
        chatService?.incomingMessagesManager?.addDialogMessageListener(dialogsMessageListener)
        dialogListeners.add(dialogListener)
    }

    override fun unsubscribeDialogListener(dialogListener: DialogListener) {
        chatService?.systemMessagesManager?.removeSystemMessageListener(systemMessagesListener)
        chatService?.incomingMessagesManager?.removeDialogMessageListrener(dialogsMessageListener)
        dialogListeners.remove(dialogListener)
    }

    override fun subscribeChatListener(chatListener: ChatListener) {
        chatService?.incomingMessagesManager?.addDialogMessageListener(chatMessageListener)
        chatListeners.add(chatListener)
    }

    override fun unsubscribeChatListener(chatListener: ChatListener) {
        chatService?.incomingMessagesManager?.removeDialogMessageListrener(chatMessageListener)
        chatListeners.remove(chatListener)
    }

    private fun addConnectionListener() {
        if (connectionListener == null) {
            connectionListener = ChatConnectionListener()
            chatService?.addConnectionListener(connectionListener)
        }
    }

    private fun removeConnectionListener() {
        chatService?.removeConnectionListener(connectionListener)
        connectionListener = null
    }

    override fun loadDialogById(dialogId: String, callback: DomainCallback<QBChatDialog, Exception>) {
        executor.addTask(object : ExecutorTask<QBChatDialog> {
            override fun backgroundWork(): QBChatDialog {
                return dialogRepository.getByIdSync(dialogId)
            }

            override fun foregroundResult(result: QBChatDialog) {
                callback.onSuccess(result, null)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    override fun createDialog(users: List<QBUser>, chatName: String, callback: DomainCallback<QBChatDialog, Exception>) {
        val dialog = buildDialog(users, chatName)
        executor.addTask(object : ExecutorTask<QBChatDialog> {
            override fun backgroundWork(): QBChatDialog {
                val createdDialog = dialogRepository.createSync(dialog)
                val chatMessage = buildMessageCreatedGroupDialog(createdDialog)
                val systemMessage = buildMessageCreatedGroupDialog(createdDialog)

                createdDialog.initForChat(chatService)
                dialogRepository.joinSync(createdDialog)

                sendSystemMessage(chatMessage, createdDialog.occupants)
                sendChatMessage(createdDialog, systemMessage)
                return createdDialog
            }

            override fun foregroundResult(result: QBChatDialog) {
                dialogs.add(result)

                callback.onSuccess(result, null)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    override fun loadMessages(dialog: QBChatDialog, skipPagination: Int, callback: DomainCallback<ArrayList<QBChatMessage>, Exception>) {
        val messageGetBuilder = QBMessageGetBuilder()
        messageGetBuilder.skip = skipPagination
        messageGetBuilder.limit = CHAT_HISTORY_ITEMS_PER_PAGE
        messageGetBuilder.sortDesc(CHAT_HISTORY_ITEMS_SORT_FIELD)
        messageGetBuilder.markAsRead(false)

        executor.addTask(object : ExecutorTask<ArrayList<QBChatMessage>> {
            override fun backgroundWork(): ArrayList<QBChatMessage> {
                val result = chatRepository.loadHistorySync(dialog, messageGetBuilder)
                return result.first
            }

            override fun foregroundResult(result: ArrayList<QBChatMessage>) {
                callback.onSuccess(result, null)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    private fun buildDialog(users: List<QBUser>, chatName: String?): QBChatDialog {
        val userIds: MutableList<Int> = ArrayList()
        for (user in users) {
            userIds.add(user.id)
        }
        val dialog = QBChatDialog()
        dialog.name = chatName
        dialog.type = QBDialogType.GROUP
        dialog.setOccupantsIds(userIds)
        return dialog
    }

    override fun loginToChat(user: QBUser?, callback: DomainCallback<Unit, Exception>) {
        if (isLoggedInChat()) {
            callback.onSuccess(Unit, null)
        } else {
            settingsRepository.applyChatSettings()
            //Need to set password, because the server will not register to chat without password
            user?.password = USER_DEFAULT_PASSWORD
            user?.let {
                executor.addTask(object : ExecutorTask<Unit> {
                    override fun backgroundWork() {
                        QBChatService.getInstance().login(it)
                    }

                    override fun foregroundResult(result: Unit) {
                        chatService = QBChatService.getInstance()
                        addConnectionListener()
                        joinDialogs(dialogs)
                        callback.onSuccess(result, null)
                    }

                    override fun onError(exception: Exception) {
                        callback.onError(exception)
                    }
                })
            }
        }
    }

    override fun loadDialogs(refresh: Boolean, reJoin: Boolean, callback: DomainCallback<ArrayList<QBChatDialog>, Exception>) {
        if (refresh) {
            skip = 0
            executor.removeTask(LOAD_DIALOGS)
        }
        val requestBuilder = QBRequestGetBuilder()
        requestBuilder.limit = DIALOGS_LIMIT
        requestBuilder.skip = skip

        if (skip != 0 && totalCount.minus(skip) == 0) {
            return
        }

        executor.addTaskWithKey(object : ExecutorTask<Pair<ArrayList<QBChatDialog>, Bundle?>> {
            override fun backgroundWork(): Pair<ArrayList<QBChatDialog>, Bundle?> {
                return dialogRepository.loadSync(requestBuilder)
            }

            override fun foregroundResult(result: Pair<ArrayList<QBChatDialog>, Bundle?>) {
                totalCount = result.second?.getInt(EXTRA_TOTAL_ENTRIES) ?: 0
                skip = result.second?.getInt(EXTRA_SKIP) ?: 0

                skip += if (totalCount.minus(skip) < DIALOGS_LIMIT) {
                    totalCount.minus(skip)
                } else {
                    DIALOGS_LIMIT
                }
                if (refresh) {
                    dialogs.clear()
                }
                dialogs.addAll(result.first)
                callback.onSuccess(result.first, null)

                if (dialogs.isEmpty()) {
                    return
                }

                if (isLoggedInChat() && reJoin) {
                    joinDialogs(dialogs)
                }

                if (dialogs.size < totalCount) {
                    loadDialogs(false, reJoin, callback)
                }
            }

            override fun onError(exception: Exception) {
                if (exception is TaskExistException) {
                    // empty
                } else {
                    callback.onError(exception)
                }
            }
        }, LOAD_DIALOGS)
    }

    override fun addUsersToDialog(dialog: QBChatDialog, users: ArraySet<QBUser>, callback: DomainCallback<QBChatDialog?, Exception>) {
        executor.addTask(object : ExecutorTask<Pair<QBChatDialog?, Bundle>> {
            override fun backgroundWork(): Pair<QBChatDialog?, Bundle> {
                val usersIds = arrayListOf<Int>()

                users.forEach { user ->
                    usersIds.add(user.id)
                }
                val message = buildMessageAddedUsers(dialog, occupantsIdsToString(usersIds), dbRepository.getCurrentUser()?.fullName
                    ?: "", getOccupantsNames(users) ?: ""
                )

                val qbRequestBuilder = QBDialogRequestBuilder()
                qbRequestBuilder.addUsers(*users.toTypedArray())
                sendChatMessage(dialog, message)
                sendSystemMessage(message, usersIds)
                return dialogRepository.updateSync(dialog, qbRequestBuilder)
            }

            override fun foregroundResult(result: Pair<QBChatDialog?, Bundle>) {
                callback.onSuccess(result.first, result.second)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    private fun getOccupantsNames(qbUsers: Collection<QBUser>): String? {
        val userNameList = arrayListOf<String>()
        for (user in qbUsers) {
            if (TextUtils.isEmpty(user.fullName)) {
                userNameList.add(user.login)
            } else {
                userNameList.add(user.fullName)
            }
        }
        return TextUtils.join(", ", userNameList)
    }

    private fun occupantsIdsToString(occupantIdsList: Collection<Int>): String {
        return TextUtils.join(",", occupantIdsList)
    }

    private fun sendChatMessage(dialog: QBChatDialog, qbChatMessage: QBChatMessage) {
        if (dialog.isJoined) {
            dialog.sendMessage(qbChatMessage)
        } else {
            dialogRepository.joinSync(dialog)
            dialog.sendMessage(qbChatMessage)
        }
    }

    private fun sendSystemMessage(message: QBChatMessage, occupants: List<Int>) {
        message.setSaveToHistory(false)
        message.isMarkable = false
        for (opponentId in occupants) {
            if (opponentId != dbRepository.getCurrentUser()?.id) {
                message.recipientId = opponentId
                chatService?.systemMessagesManager?.sendSystemMessage(message)
            }
        }
    }

    override fun deleteDialogs(dialogsDelete: ArrayList<QBChatDialog>, qbUser: QBUser, callback: DomainCallback<List<QBChatDialog>, Exception>) {
        val qbRequestBuilder = QBDialogRequestBuilder()
        qbRequestBuilder.removeUsers(qbUser)
        val size = dialogsDelete.size
        var responseCounter = 0

        for (dialog in dialogsDelete) {
            executor.addTask(object : ExecutorTask<Pair<QBChatDialog?, Bundle>> {
                override fun backgroundWork(): Pair<QBChatDialog?, Bundle> {
                    val message = buildMessageLeftUser(dialog)
                    sendChatMessage(dialog, message)
                    return dialogRepository.updateSync(dialog, qbRequestBuilder)
                }

                override fun foregroundResult(result: Pair<QBChatDialog?, Bundle>) {
                    dialogsDelete.remove(result.first)
                    dialogs.remove(result.first)
                    if (++responseCounter == size) {
                        callback.onSuccess(ArrayList<QBChatDialog>(dialogsDelete), result.second)
                    }
                }

                override fun onError(exception: Exception) {
                    if (++responseCounter == size) {
                        callback.onSuccess(ArrayList<QBChatDialog>(dialogsDelete), null)
                    }
                }
            })
        }
    }

    // TODO: 6/9/21 Need to add only 1 logic for leave from dialog
    override fun leaveDialog(dialog: QBChatDialog, qbUser: QBUser, callback: DomainCallback<QBChatDialog, Exception>) {
        val qbRequestBuilder = QBDialogRequestBuilder()
        qbRequestBuilder.removeUsers(qbUser)

        executor.addTask(object : ExecutorTask<Pair<QBChatDialog?, Bundle>> {
            override fun backgroundWork(): Pair<QBChatDialog?, Bundle> {
                val chatMessage = buildMessageLeftUser(dialog)
                val systemMessage = buildMessageLeftUser(dialog)
                dialog.leave()
                sendChatMessage(dialog, chatMessage)
                sendSystemMessage(systemMessage, dialog.occupants)
                return dialogRepository.updateSync(dialog, qbRequestBuilder)
            }

            override fun foregroundResult(result: Pair<QBChatDialog?, Bundle>) {
                result.first?.let { callback.onSuccess(it, result.second) }
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    override fun readMessage(qbChatMessage: QBChatMessage, qbDialog: QBChatDialog, callback: DomainCallback<Unit, Exception>) {
        executor.addTask(object : ExecutorTask<Unit> {
            override fun backgroundWork() {
                return qbDialog.readMessage(qbChatMessage)
            }

            override fun foregroundResult(result: Unit) {
                callback.onSuccess(result, null)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    override fun sendCreateConference(dialog: QBChatDialog, callback: DomainCallback<Unit, Exception>) {
        val message = buildMessageConferenceStarted(dialog)
        executor.addTask(object : ExecutorTask<Unit> {
            override fun backgroundWork() {
                return sendChatMessage(dialog, message)
            }

            override fun foregroundResult(result: Unit) {
                // empty
            }

            override fun onError(exception: Exception) {
                // empty
            }
        })
    }

    override fun buildAndSendStartStreamMessage(dialog: QBChatDialog, streamId: String, callback: DomainCallback<Unit, Exception>) {
        val message = buildMessageStreamStarted(dialog, streamId)
        executor.addTask(object : ExecutorTask<Unit> {
            override fun backgroundWork() {
                return sendChatMessage(dialog, message)
            }

            override fun foregroundResult(result: Unit) {
                // empty
            }

            override fun onError(exception: Exception) {
                // empty
            }
        })
    }

    override fun buildAndSendMessage(currentUser: QBUser, dialog: QBChatDialog, text: String, attachmentModels: ArrayList<AttachmentModel>, callback: DomainCallback<Unit, Exception>) {
        if (isLoggedInChat()) {
            send(text, dialog, attachmentModels, callback)
        } else {
            loginToChat(currentUser, object : DomainCallback<Unit, Exception> {
                override fun onSuccess(result: Unit, bundle: Bundle?) {
                    send(text, dialog, attachmentModels, callback)
                }

                override fun onError(error: Exception) {
                    callback.onError(error)
                }
            })
        }
    }

    override fun getDialogs(): ArrayList<QBChatDialog> {
        return dialogs
    }

    override fun clearDialogs() {
        totalCount = 0
        skip = 0
        dialogs.clear()
    }

    private fun send(text: String, qbDialog: QBChatDialog?, attachmentModels: ArrayList<AttachmentModel>, callback: DomainCallback<Unit, Exception>) {
        qbDialog?.let { dialog ->
            chatService?.let {
                qbDialog.initForChat(it)
            }
            executor.addTask(object : ExecutorTask<Unit> {
                override fun backgroundWork() {
                    if (attachmentModels.isNotEmpty()) {
                        for (attachmentModel in attachmentModels) {
                            try {
                                dialog.join(DiscussionHistory())
                                attachmentModel.qbFile?.let {
                                    dialog.sendMessage(buildAttachmentMessage(it))
                                }
                                attachmentModels.remove(attachmentModel)
                            } catch (exception: Exception) {
                                callback.onError(exception)
                            }
                        }
                    }
                    if (text.isNotEmpty()) {
                        try {
                            dialog.join(DiscussionHistory())
                            dialog.sendMessage(buildTextMessage(text))
                        } catch (exception: SmackException.NotConnectedException) {
                            callback.onError(exception)
                        }
                    }
                    return
                }

                override fun foregroundResult(result: Unit) {
                    callback.onSuccess(result, null)
                }

                override fun onError(exception: Exception) {
                    callback.onError(exception)
                }
            })
        }
    }

    private fun buildMessageAddedUsers(dialog: QBChatDialog?, userIds: String, currentUserName: String, usersNames: String?): QBChatMessage {
        val qbChatMessage = QBChatMessage()
        qbChatMessage.dialogId = dialog?.dialogId
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, OCCUPANTS_ADDED)
        qbChatMessage.setProperty(PROPERTY_NEW_OCCUPANTS_IDS, userIds)
        qbChatMessage.body = resourcesManager.get().getString(R.string.occupant_added, currentUserName, usersNames)
        qbChatMessage.setSaveToHistory(true)
        qbChatMessage.isMarkable = true
        return qbChatMessage
    }

    private fun buildMessageLeftUser(dialog: QBChatDialog): QBChatMessage {
        val qbChatMessage = QBChatMessage()
        qbChatMessage.dialogId = dialog.dialogId
        qbChatMessage.senderId = dbRepository.getCurrentUser()?.id
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, OCCUPANT_LEFT)
        qbChatMessage.body = resourcesManager.get().getString(R.string.occupant_left, dbRepository.getCurrentUser()?.fullName)
        qbChatMessage.setSaveToHistory(true)
        qbChatMessage.isMarkable = true
        return qbChatMessage
    }

    private fun buildMessageCreatedGroupDialog(dialog: QBChatDialog): QBChatMessage {
        val qbChatMessage = QBChatMessage()
        qbChatMessage.dialogId = dialog.dialogId
        qbChatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, occupantsIdsToString(dialog.occupants))
        qbChatMessage.setProperty(PROPERTY_DIALOG_TYPE, dialog.type.code.toString())
        qbChatMessage.setProperty(PROPERTY_DIALOG_NAME, dialog.name.toString())
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, CREATING_DIALOG)
        qbChatMessage.dateSent = System.currentTimeMillis() / 1000
        qbChatMessage.body = resourcesManager.get().getString(
                R.string.new_chat_created, dbRepository.getCurrentUser()?.fullName
            ?: dbRepository.getCurrentUser()?.login, dialog.name
        )
        qbChatMessage.setSaveToHistory(true)
        qbChatMessage.isMarkable = true
        return qbChatMessage
    }

    private fun buildAttachmentMessage(qbFile: QBFile): QBChatMessage {
        val chatMessage = QBChatMessage()
        chatMessage.addAttachment(buildAttachment(qbFile))
        chatMessage.setSaveToHistory(true)
        chatMessage.dateSent = System.currentTimeMillis() / 1000
        chatMessage.isMarkable = true
        return chatMessage
    }

    private fun buildTextMessage(text: String): QBChatMessage {
        val chatMessage = QBChatMessage()
        chatMessage.body = text
        chatMessage.setSaveToHistory(true)
        chatMessage.dateSent = System.currentTimeMillis() / 1000
        chatMessage.isMarkable = true
        return chatMessage
    }

    private fun buildAttachment(qbFile: QBFile): QBAttachment {
        val type = QBAttachment.IMAGE_TYPE
        val attachment = QBAttachment(type)
        attachment.id = qbFile.uid
        attachment.size = qbFile.size.toDouble()
        attachment.name = qbFile.name
        attachment.contentType = qbFile.contentType
        return attachment
    }

    private fun buildMessageConferenceStarted(dialog: QBChatDialog): QBChatMessage {
        val qbChatMessage = QBChatMessage()
        qbChatMessage.dialogId = dialog.dialogId
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, START_CONFERENCE)
        qbChatMessage.setProperty(PROPERTY_CONVERSATION_ID, dialog.dialogId)
        qbChatMessage.body = resourcesManager.get().getString(R.string.chat_conversation_started, resourcesManager.get().getString(R.string.conference))
        qbChatMessage.setSaveToHistory(true)
        qbChatMessage.isMarkable = true
        return qbChatMessage
    }

    private fun buildMessageStreamStarted(dialog: QBChatDialog, streamId: String): QBChatMessage {
        val qbChatMessage = QBChatMessage()
        qbChatMessage.dialogId = dialog.dialogId
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, START_STREAM)
        qbChatMessage.setProperty(PROPERTY_CONVERSATION_ID, streamId)
        qbChatMessage.body = resourcesManager.get().getString(R.string.started_stream)
        qbChatMessage.setSaveToHistory(true)
        qbChatMessage.isMarkable = true
        return qbChatMessage
    }

    override fun destroyChat() {
        removeConnectionListener()
        chatService?.destroy()
        chatService = null
    }

    override fun isLoggedInChat(): Boolean {
        chatService?.let {
            return it.isLoggedIn
        }
        return false
    }

    override fun joinDialogs(dialogs: List<QBChatDialog>) {
        executor.addTask(object : ExecutorTask<Unit> {
            override fun backgroundWork() {
                for (dialog in dialogs) {
                    dialogRepository.joinSync(dialog)
                }
            }

            override fun foregroundResult(result: Unit) {
                // empty
            }

            override fun onError(exception: Exception) {
                // empty
            }
        })
    }

    override fun joinDialog(dialog: QBChatDialog, callback: DataCallBack<Unit?, Exception>) {
        dialog.initForChat(chatService)

        if (dialog.isJoined) {
            callback.onSuccess(Unit, null)
            return
        }

        executor.addTask(object : ExecutorTask<Unit> {
            override fun backgroundWork() {
                return dialogRepository.joinSync(dialog)
            }

            override fun foregroundResult(result: Unit) {
                callback.onSuccess(result, null)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    inner class ChatConnectionListener : ConnectionListener {
        override fun connected(p0: XMPPConnection?) {
            //empty
        }

        override fun authenticated(p0: XMPPConnection?, p1: Boolean) {
            //empty
        }

        override fun connectionClosed() {
            chatService = null
        }

        override fun connectionClosedOnError(exception: Exception) {
            connectionChatListeners.forEach { listener ->
                listener.onError(exception)
            }
        }

        override fun reconnectionSuccessful() {
            chatService = QBChatService.getInstance()
            for (dialog in dialogs) {
                dialog.initForChat(chatService)
            }
            joinDialogs(dialogs)
            connectionChatListeners.forEach { listener ->
                listener.onConnectedChat()
            }
        }

        override fun reconnectingIn(p0: Int) {
            //empty
        }

        override fun reconnectionFailed(exception: Exception) {
            connectionChatListeners.forEach { listener ->
                listener.reconnectionFailed(exception)
            }
            chatService = null
        }
    }

    private inner class SystemMessagesListener : QBSystemMessageListener {
        override fun processMessage(qbChatMessage: QBChatMessage) {
            executor.addTask(object : ExecutorTask<QBChatDialog> {
                override fun backgroundWork(): QBChatDialog {
                    // TODO: 5/17/21 Delay for show message
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    val dialog = dialogRepository.getByIdSync(qbChatMessage.dialogId)
                    if (qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE) == CREATING_DIALOG) {
                        dialogRepository.joinSync(dialog)
                    }
                    return dialog
                }

                override fun foregroundResult(result: QBChatDialog) {
                    dialogListeners.forEach { listener ->
                        listener.onUpdatedDialog(result)
                    }
                }

                override fun onError(exception: Exception) {
                    dialogListeners.forEach { listener ->
                        listener.onError(exception)
                    }
                }
            })
        }

        override fun processError(exception: QBChatException, qbChatMessage: QBChatMessage) {
            dialogListeners.forEach { listener ->
                listener.onError(exception)
            }
        }
    }

    private inner class ChatMessageListener : QBChatDialogMessageListener {
        override fun processMessage(dialogId: String, chatMessage: QBChatMessage, senderId: Int) {
            when (chatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE)) {
                OCCUPANT_LEFT -> {
                    if (dbRepository.getCurrentUser()?.id != senderId) {
                        loadDialogWithJoin(dialogId, chatMessage)
                    }
                }
                OCCUPANTS_ADDED -> {
                    loadDialogWithJoin(dialogId, chatMessage)
                }
                else -> {
                    chatListeners.forEach { listener ->
                        listener.onReceivedMessage(dialogId, chatMessage, false)
                    }
                }
            }
        }

        override fun processError(dialogId: String?, exception: QBChatException?, qbChatMessage: QBChatMessage?, userId: Int?) {
            exception?.message?.let {
                chatListeners.forEach { listener ->
                    listener.onError(exception)
                }
            }
        }

        private fun loadDialogWithJoin(dialogId: String, chatMessage: QBChatMessage) {
            executor.addTask(object : ExecutorTask<Unit> {
                override fun backgroundWork() {
                    for ((index, dialog) in dialogs.withIndex()) {
                        if (dialog.dialogId == dialogId) {
                            val updatedDialog = dialogRepository.getByIdSync(dialogId)
                            dialogs[index] = updatedDialog
                            dialogRepository.joinSync(updatedDialog)
                            break
                        }
                    }
                }

                override fun foregroundResult(result: Unit) {
                    chatListeners.forEach { listener ->
                        listener.onReceivedMessage(dialogId, chatMessage, true)
                    }
                }

                override fun onError(exception: Exception) {
                    chatListeners.forEach { listener ->
                        listener.onError(exception)
                    }
                }
            })
        }
    }

    private inner class DialogsMessageListener : QBChatDialogMessageListener {
        override fun processMessage(dialogId: String, qbChatMessage: QBChatMessage, senderId: Int) {
            if (qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE) == OCCUPANT_LEFT &&
                senderId == dbRepository.getCurrentUser()?.id) {
                return
            }
            executor.addTask(object : ExecutorTask<QBChatDialog> {
                override fun backgroundWork(): QBChatDialog {
                    val dialog = dialogRepository.getByIdSync(qbChatMessage.dialogId)
                    dialogRepository.joinSync(dialog)
                    return dialog
                }

                override fun foregroundResult(result: QBChatDialog) {
                    dialogListeners.forEach { listener ->
                        listener.onUpdatedDialog(result)
                    }
                }

                override fun onError(exception: Exception) {
                    dialogListeners.forEach { listener ->
                        listener.onError(exception)
                    }
                }
            })
        }

        override fun processError(dialogId: String?, exception: QBChatException?, qbChatMessage: QBChatMessage?, userId: Int?) {
            exception?.message?.let {
                dialogListeners.forEach { listener ->
                    listener.onError(exception)
                }
            }
        }
    }
}