package com.quickblox.sample.conference.kotlin.presentation.screens.chat

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.annotation.IntDef
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.conference.QBConferenceRole
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.LoadFileCallBack
import com.quickblox.sample.conference.kotlin.domain.call.CallManager
import com.quickblox.sample.conference.kotlin.domain.call.CallManagerImpl.Companion.CallType.Companion.CONVERSATION
import com.quickblox.sample.conference.kotlin.domain.call.CallManagerImpl.Companion.CallType.Companion.STREAM
import com.quickblox.sample.conference.kotlin.domain.chat.CHAT_HISTORY_ITEMS_PER_PAGE
import com.quickblox.sample.conference.kotlin.domain.chat.ChatListener
import com.quickblox.sample.conference.kotlin.domain.chat.ChatManager
import com.quickblox.sample.conference.kotlin.domain.chat.ConnectionChatListener
import com.quickblox.sample.conference.kotlin.domain.files.FileManager
import com.quickblox.sample.conference.kotlin.domain.files.ProgressCallback
import com.quickblox.sample.conference.kotlin.domain.repositories.connection.ConnectionRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.connection.ConnectivityChangedListener
import com.quickblox.sample.conference.kotlin.domain.user.UserManager
import com.quickblox.sample.conference.kotlin.presentation.LiveData
import com.quickblox.sample.conference.kotlin.presentation.SuccessResult
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManager
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseViewModel
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ChatViewModel.Companion.MessageType.Companion.HEADER
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ChatViewModel.Companion.MessageType.Companion.MESSAGE
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.ERROR_LOAD_ATTACHMENT
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.ERROR_UPLOAD
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.FILE_DELETED
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.FILE_LOADED
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.FILE_SHOWED
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.LEAVE
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.LOADER_PROGRESS_UPDATED
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.MESSAGES_SHOWED
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.MESSAGE_SENT
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.RECEIVED_MESSAGE
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.SHOW_ATTACHMENT_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.SHOW_CALL_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.SHOW_INFO_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.SHOW_LOGIN_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.UPDATE_TOOLBAR
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.attachment.AttachmentModel
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message.ChatMessage
import com.quickblox.users.model.QBUser
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val CAMERA_FILE_NAME_PREFIX = "CAMERA_"
private const val JPG = ".jpg"

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val userManager: UserManager, private val chatManager: ChatManager, private val fileManager: FileManager,
    private val resourcesManager: ResourcesManager, private val connectionRepository: ConnectionRepository,
    private val callManager: CallManager
) : BaseViewModel() {
    private val TAG: String = ChatViewModel::class.java.simpleName
    val messages = arrayListOf<ChatMessage>()
    val liveData = LiveData<Pair<Int, Any?>>()
    private val connectionListener = ConnectionListener(TAG)
    private val chatListener = ChatListenerImpl(TAG)
    private val usersDialog = hashSetOf<QBUser>()
    private val connectivityChangedListener = ConnectivityChangedListenerImpl(TAG)

    var currentUser: QBUser? = null
        private set

    var currentDialog: QBChatDialog? = null
        private set

    var skipPagination = 0
        private set

    companion object {
        @IntDef(MESSAGE, HEADER)
        annotation class MessageType {
            companion object {
                const val MESSAGE = 0
                const val HEADER = 1
            }
        }
    }

    init {
        fileManager.getAttachmentsList().clear()
        currentUser = userManager.getCurrentUser()
        subscribeChatListener()
    }

    fun loadDialogById(dialogId: String) {
        for (dialog in chatManager.getDialogs()) {
            if (dialog.dialogId == dialogId) {
                currentDialog = dialog
                loadMessages()
                break
            }
        }

        if (currentDialog == null) {
            chatManager.loadDialogById(dialogId, object : DomainCallback<QBChatDialog, Exception> {
                override fun onSuccess(result: QBChatDialog, bundle: Bundle?) {
                    currentDialog = result
                    liveData.setValue(Pair(UPDATE_TOOLBAR, null))
                    loadMessages()
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        }
    }

    fun getUsersDialog(): HashSet<QBUser> {
        return usersDialog
    }

    fun getAttachments(): ArrayList<AttachmentModel> {
        return fileManager.getAttachmentsList()
    }

    fun loadMessages() {
        liveData.setValue(Pair(PROGRESS, null))
        currentDialog?.let {
            chatManager.loadMessages(it, skipPagination, object : DomainCallback<ArrayList<QBChatMessage>, Exception> {
                override fun onSuccess(messages: ArrayList<QBChatMessage>, bundle: Bundle?) {
                    loadUsersFromMessages(messages, object : SuccessResult<Unit> {
                        override fun onSuccess(result: Unit?) {
                            showMessages(messages)
                        }
                    })
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        }
    }

    private fun loginToChat(user: QBUser?) {
        liveData.setValue(Pair(PROGRESS, null))
        user?.let {
            chatManager.loginToChat(it, object : DomainCallback<Unit, Exception> {
                override fun onSuccess(result: Unit, bundle: Bundle?) {
                    currentDialog?.let { joinDialog(it) }
                    subscribeChatListener()
                    skipPagination = 0
                    loadMessages()
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        } ?: run {
            liveData.setValue(Pair(SHOW_LOGIN_SCREEN, null))
        }
    }

    private fun addHeaders(messages: ArrayList<QBChatMessage>): ArrayList<ChatMessage> {
        val messagesList = arrayListOf<ChatMessage>()
        for ((index, message) in messages.withIndex()) {
            if (index < messages.size - 1) {
                messagesList.add(ChatMessage(message))
                if (isAddHeader(messages[index], messages[index + 1])) {
                    val messageHeader = QBChatMessage()
                    messageHeader.id = index.toString()
                    messageHeader.dateSent = message.dateSent
                    messagesList.add(ChatMessage(messageHeader, HEADER))
                }
            } else {
                if (messages.size < 50) {
                    messagesList.add(ChatMessage(message))
                    val messageHeader = QBChatMessage()
                    messageHeader.id = index.toString()
                    messageHeader.dateSent = message.dateSent
                    messagesList.add(ChatMessage(messageHeader, HEADER))
                }
            }
        }
        return messagesList
    }

    private fun isAddHeader(message: QBChatMessage, previousMessage: QBChatMessage): Boolean {
        val milliseconds: Long = message.dateSent * 1000
        val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date(milliseconds)).toLong()
        val previousMilliseconds: Long = previousMessage.dateSent * 1000
        val previousDate = dateFormat.format(Date(previousMilliseconds)).toLong()

        return currentDate > previousDate
    }

    private fun loadUsersFromMessages(messages: ArrayList<QBChatMessage>, callback: SuccessResult<Unit>) {
        val needLoadOccupants = hashSetOf<Int>()

        messages.forEach {
            if (!usersDialog.contains(QBUser(it.senderId))) {
                needLoadOccupants.add(it.senderId)
            }
        }
        currentDialog?.occupants?.forEach {
            if (!usersDialog.contains(QBUser(it))) {
                needLoadOccupants.add(it)
            }
        }

        if (needLoadOccupants.isEmpty()) {
            callback.onSuccess(Unit)
        } else {
            userManager.loadUsersByIds(needLoadOccupants, object : DomainCallback<ArrayList<QBUser>, Exception> {
                override fun onSuccess(result: ArrayList<QBUser>, bundle: Bundle?) {
                    usersDialog.addAll(result)
                    callback.onSuccess(Unit)
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        }
    }

    private fun showMessages(messagesList: ArrayList<QBChatMessage>) {
        val newMessages = addHeaders(messagesList)
        newMessages.reverse()
        if (skipPagination == 0) {
            messages.clear()
        }
        messages.addAll(0, newMessages)
        liveData.setValue(Pair(MESSAGES_SHOWED, newMessages.size))
        skipPagination += CHAT_HISTORY_ITEMS_PER_PAGE
    }

    private fun joinDialog(dialog: QBChatDialog) {
        chatManager.joinDialog(dialog, object : DataCallBack<Unit?, Exception> {
            override fun onSuccess(result: Unit?, bundle: Bundle?) {
                // empty
            }

            override fun onError(error: Exception) {
                liveData.setValue(Pair(ERROR, error.message))
            }
        })
    }

    private fun subscribeChatListener() {
        chatManager.subscribeChatListener(chatListener)
        chatManager.subscribeConnectionChatListener(connectionListener)
    }

    override fun onResumeView() {
        connectionRepository.addListener(connectivityChangedListener)
        if (!chatManager.isLoggedInChat()) {
            loginToChat(currentUser)
        } else {
            currentDialog?.let { joinDialog(it) }
            subscribeChatListener()
        }
    }

    fun unsubscribe() {
        connectionRepository.removeListener(connectivityChangedListener)
        chatManager.unsubscribeChatListener(chatListener)
        chatManager.unSubscribeConnectionChatListener(connectionListener)
    }

    override fun onStopApp() {
        unsubscribe()
        chatManager.destroyChat()
    }

    fun uploadFile(uri: Uri) {
        fileManager.upload(uri, object : LoadFileCallBack<AttachmentModel?, Exception> {
            override fun onError(error: Exception) {
                liveData.setValue(Pair(ERROR_LOAD_ATTACHMENT, error.message))
            }

            override fun onLoaded() {
                liveData.setValue(Pair(FILE_LOADED, null))
            }

            override fun onCreated(result: AttachmentModel?) {
                liveData.setValue(Pair(FILE_SHOWED, null))
            }
        }, object : ProgressCallback {
            override fun onChangeProgress() {
                liveData.setValue(Pair(LOADER_PROGRESS_UPDATED, null))
            }
        })
    }

    fun uploadFile(file: File) {
        fileManager.upload(file, object : LoadFileCallBack<AttachmentModel?, Exception> {
            override fun onError(error: Exception) {
                liveData.setValue(Pair(ERROR_UPLOAD, error.message))
            }

            override fun onLoaded() {
                liveData.setValue(Pair(FILE_LOADED, null))
            }

            override fun onCreated(result: AttachmentModel?) {
                liveData.setValue(Pair(FILE_SHOWED, null))
            }
        }, object : ProgressCallback {
            override fun onChangeProgress() {
                liveData.setValue(Pair(LOADER_PROGRESS_UPDATED, null))
            }
        })
    }

    fun sendMessage(text: String) {
        // TODO: 7/6/21 Need to handle error
        currentDialog?.let { dialog ->
            currentUser?.let { user ->
                chatManager.buildAndSendMessage(user, dialog, text, fileManager.getAttachmentsList(), object : DomainCallback<Unit, Exception> {
                    override fun onSuccess(result: Unit, bundle: Bundle?) {
                        fileManager.getAttachmentsList().clear()
                        liveData.setValue(Pair(MESSAGE_SENT, null))
                    }

                    override fun onError(error: Exception) {
                        liveData.setValue(Pair(ERROR, error))
                    }
                })
            }
        }
    }

    fun getTemporaryCameraFileName(): String {
        return CAMERA_FILE_NAME_PREFIX + System.currentTimeMillis() + JPG
    }

    fun removeQBFile(attachmentModel: AttachmentModel) {
        fileManager.delete(attachmentModel, object : DomainCallback<Void?, Exception> {
            override fun onSuccess(result: Void?, bundle: Bundle?) {
                liveData.setValue(Pair(FILE_DELETED, null))
            }

            override fun onError(error: Exception) {
                liveData.setValue(Pair(ERROR, error.message))
            }
        })
    }

    fun readMessage(qbChatMessage: QBChatMessage) {
        currentDialog?.let { dialog ->
            chatManager.readMessage(qbChatMessage, dialog, object : DomainCallback<Unit, Exception> {
                override fun onSuccess(result: Unit, bundle: Bundle?) {
                    // empty
                }

                override fun onError(error: Exception) {
                    error.message?.let { errorMessage -> Log.d(TAG, errorMessage) }
                }
            })
        }
    }

    fun showAttachmentScreen(url: String) {
        liveData.setValue(Pair(SHOW_ATTACHMENT_SCREEN, url))
    }

    fun leaveGroupChat() {
        if (!connectionRepository.isInternetAvailable()) {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.no_internet)))
            return
        }
        currentDialog?.let { dialog ->
            currentUser?.let { user ->
                chatManager.leaveDialog(dialog, user, object : DomainCallback<QBChatDialog, Exception> {
                    override fun onSuccess(result: QBChatDialog, bundle: Bundle?) {
                        liveData.setValue(Pair(LEAVE, null))
                    }

                    override fun onError(error: Exception) {
                        liveData.setValue(Pair(ERROR, error.message))
                    }
                })
            }
        }
    }

    fun startConference() {
        starCall(QBConferenceRole.PUBLISHER, CONVERSATION, currentDialog?.dialogId)
        sendCreateConference()
    }

    fun checkExistSessionAndJoinConference(conversationId: String) {
        if (callManager.getSession() == null) {
            starCall(QBConferenceRole.PUBLISHER, CONVERSATION, currentDialog?.dialogId)
        } else {
            if (callManager.getSession()?.dialogID == conversationId){
                liveData.setValue(Pair(SHOW_CALL_SCREEN, null))
                return
            }
            callManager.leaveFromSession(object : DomainCallback<Unit, Exception> {
                override fun onSuccess(result: Unit, bundle: Bundle?) {
                    starCall(QBConferenceRole.PUBLISHER, CONVERSATION, currentDialog?.dialogId)
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        }
    }

    fun startStream() {
        val streamId = createNewStreamId()
        starCall(QBConferenceRole.PUBLISHER, STREAM, streamId)
        buildAndSendStartStreamMessage(streamId)
    }

    fun checkExistSessionAndJoinStream(senderId: Int?, conversationId: String) {
        if (callManager.getSession() == null) {
            val role = checkRole(senderId)
            starCall(role, STREAM, conversationId)
        } else {
            if (callManager.getSession()?.dialogID == conversationId){
                liveData.setValue(Pair(SHOW_CALL_SCREEN, null))
                return
            }
            callManager.leaveFromSession(object : DomainCallback<Unit, Exception> {
                override fun onSuccess(result: Unit, bundle: Bundle?) {
                    val role = checkRole(senderId)
                    starCall(role, STREAM, conversationId)
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        }
    }

    private fun checkRole(senderId: Int?): QBConferenceRole {
        return if (currentUser?.id == senderId) {
            QBConferenceRole.PUBLISHER
        } else {
            QBConferenceRole.LISTENER
        }
    }

    private fun sendCreateConference() {
        currentDialog?.let {
            chatManager.sendCreateConference(it, object : DomainCallback<Unit, Exception> {
                override fun onSuccess(result: Unit, bundle: Bundle?) {
                    // empty
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        }
    }

    private fun buildAndSendStartStreamMessage(streamId: String) {
        currentDialog?.let {
            chatManager.buildAndSendStartStreamMessage(it, streamId, object : DomainCallback<Unit, Exception> {
                override fun onSuccess(result: Unit, bundle: Bundle?) {
                    // empty
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        }
    }

    private fun createNewStreamId(): String {
        val currentUserId = currentUser?.id
        val timeStamp = System.currentTimeMillis()
        return currentUserId.toString() + "_" + timeStamp
    }

    private fun starCall(role: QBConferenceRole, callType: Int, roomId: String?) {
        if (!connectionRepository.isInternetAvailable()) {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.no_internet)))
            return
        }

        val currentUserId = currentUser?.id
        currentUserId?.let { id ->
            callManager.createAndJoinSession(id, currentDialog, roomId, role, callType, object : DomainCallback<Unit, Exception> {
                    override fun onSuccess(result: Unit, bundle: Bundle?) {
                        liveData.setValue(Pair(SHOW_CALL_SCREEN, null))
                    }

                    override fun onError(error: Exception) {
                        liveData.setValue(Pair(ERROR, error.message))
                    }
                })
        } ?: run {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.userId_error)))
        }
    }

    fun showChatInfoScreen() {
        if (!connectionRepository.isInternetAvailable()) {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.no_internet)))
            return
        }
        liveData.setValue(Pair(SHOW_INFO_SCREEN, null))
    }

    private inner class ConnectionListener(val tag: String) : ConnectionChatListener {
        override fun onConnectedChat() {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.connected)))
        }

        override fun onError(exception: Exception) {
            liveData.setValue(Pair(ERROR, exception.message))
        }

        override fun reconnectionFailed(exception: Exception) {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.reconnecting)))
            loginToChat(currentUser)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is ConnectionListener) {
                return false
            }
            return tag == other.tag
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }

    private inner class ChatListenerImpl(val tag: String) : ChatListener {
        override fun onReceivedMessage(dialogId: String, message: QBChatMessage, updatedDialog: Boolean) {
            if (currentDialog?.dialogId == dialogId && messages.isNotEmpty()) {
                if (isAddHeader(message, messages[messages.size - 1].qbChatMessage)) {
                    val messageHeader = QBChatMessage()
                    messageHeader.id = messages.size.toString()
                    messageHeader.dateSent = message.dateSent
                    messages.add(ChatMessage(messageHeader, HEADER))
                }
                if (!TextUtils.isEmpty(message.body) || message.attachments.isNotEmpty()) {
                    messages.add(ChatMessage(message))
                    if (updatedDialog) {
                        currentDialog = chatManager.getDialogs().find { it.dialogId == dialogId }
                    }
                    liveData.setValue(Pair(RECEIVED_MESSAGE, updatedDialog))
                }
            }
        }

        override fun onError(exception: Exception) {
            liveData.setValue(Pair(ERROR, exception.message))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is ChatListenerImpl) {
                return false
            }

            return tag == other.tag
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }

    private inner class ConnectivityChangedListenerImpl(val tag: String) : ConnectivityChangedListener {
        override fun onAvailable() {
            if (!chatManager.isLoggedInChat()) {
                loginToChat(currentUser)
                liveData.setValue(Pair(UPDATE_TOOLBAR, null))
            }
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.internet_restored)))
        }

        override fun onLost() {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.no_internet)))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is ConnectivityChangedListenerImpl) {
                return false
            }
            return tag == other.tag
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }
}