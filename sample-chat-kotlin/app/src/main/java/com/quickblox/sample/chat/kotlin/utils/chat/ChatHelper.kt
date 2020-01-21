package com.quickblox.sample.chat.kotlin.utils.chat

import android.os.Bundle
import android.util.Log
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.listeners.QBChatDialogParticipantListener
import com.quickblox.chat.model.*
import com.quickblox.chat.request.QBDialogRequestBuilder
import com.quickblox.chat.request.QBMessageGetBuilder
import com.quickblox.content.QBContent
import com.quickblox.content.model.QBFile
import com.quickblox.core.LogLevel
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.QBProgressCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.sample.chat.kotlin.*
import com.quickblox.sample.chat.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.chat.kotlin.utils.qb.*
import com.quickblox.sample.chat.kotlin.utils.qb.callback.QbEntityCallbackTwoTypeWrapper
import com.quickblox.sample.chat.kotlin.utils.qb.callback.QbEntityCallbackWrapper
import com.quickblox.sample.chat.kotlin.utils.shortToast
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smackx.muc.DiscussionHistory
import java.io.File

const val CHAT_HISTORY_ITEMS_PER_PAGE = 50
const val USERS_PER_PAGE = 100
const val TOTAL_PAGES_BUNDLE_PARAM = "total_pages"
const val CURRENT_PAGE_BUNDLE_PARAM = "current_page"
private const val CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent"

object ChatHelper {
    private val TAG = ChatHelper::class.java.simpleName

    private var qbChatService: QBChatService = QBChatService.getInstance()
    private var usersLoadedFromDialog = ArrayList<QBUser>()
    private var usersLoadedFromDialogs = ArrayList<QBUser>()
    private var usersLoadedFromMessage = ArrayList<QBUser>()
    private var usersLoadedFromMessages = ArrayList<QBUser>()

    init {
        QBSettings.getInstance().logLevel = LogLevel.DEBUG
        QBChatService.setDebugEnabled(true)
        QBChatService.setConfigurationBuilder(buildChatConfigs())
        QBChatService.setDefaultPacketReplyTimeout(10000)
        qbChatService.setUseStreamManagement(true)
    }

    private fun buildChatConfigs(): QBChatService.ConfigurationBuilder {
        val configurationBuilder = QBChatService.ConfigurationBuilder()

        configurationBuilder.socketTimeout = SOCKET_TIMEOUT
        configurationBuilder.isUseTls = USE_TLS
        configurationBuilder.isKeepAlive = KEEP_ALIVE
        configurationBuilder.isAutojoinEnabled = AUTO_JOIN
        configurationBuilder.setAutoMarkDelivered(AUTO_MARK_DELIVERED)
        configurationBuilder.isReconnectionAllowed = RECONNECTION_ALLOWED
        configurationBuilder.setAllowListenNetwork(ALLOW_LISTEN_NETWORK)
        configurationBuilder.port = CHAT_PORT

        return configurationBuilder
    }

    private fun buildDialogNameWithoutUser(dialogName: String, userName: String): String {
        val regex = ", $userName|$userName, "
        return dialogName.replace(regex.toRegex(), "")
    }

    fun isLogged(): Boolean {
        return QBChatService.getInstance().isLoggedIn
    }

    fun getCurrentUser(): QBUser? {
        if (SharedPrefsHelper.hasQbUser()) {
            return SharedPrefsHelper.getQbUser()!!
        } else {
            return null
        }
    }

    fun addConnectionListener(listener: ConnectionListener?) {
        qbChatService.addConnectionListener(listener)
    }

    fun removeConnectionListener(listener: ConnectionListener?) {
        qbChatService.removeConnectionListener(listener)
    }

    fun updateUser(user: QBUser, callback: QBEntityCallback<QBUser>) {
        QBUsers.updateUser(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(user: QBUser, bundle: Bundle) {
                callback.onSuccess(user, bundle)
            }

            override fun onError(e: QBResponseException) {
                callback.onError(e)
            }
        })
    }

    fun login(user: QBUser, callback: QBEntityCallback<QBUser>) {
        // Create REST API session on QuickBlox
        QBUsers.signIn(user).performAsync(object : QbEntityCallbackTwoTypeWrapper<QBUser, QBUser>(callback) {
            override fun onSuccess(qbUser: QBUser, bundle: Bundle?) {
                callback.onSuccess(qbUser, bundle)
            }
        })
    }

    fun loginToChat(user: QBUser, callback: QBEntityCallback<Void>) {
        if (!qbChatService.isLoggedIn) {
            qbChatService.login(user, callback)
        } else {
            callback.onSuccess(null, null)
        }
    }

    fun join(chatDialog: QBChatDialog, callback: QBEntityCallback<Void>) {
        val history = DiscussionHistory()
        history.maxStanzas = 0
        chatDialog.join(history, callback)
    }

    @Throws(Exception::class)
    fun join(dialogs: List<QBChatDialog>) {
        for (dialog in dialogs) {
            val history = DiscussionHistory()
            history.maxStanzas = 0
            dialog.join(history)
        }
    }

    @Throws(XMPPException::class, SmackException.NotConnectedException::class)
    fun leaveChatDialog(chatDialog: QBChatDialog) {
        chatDialog.leave()
    }

    fun destroy() {
        qbChatService.destroy()
    }

    fun createDialogWithSelectedUsers(users: MutableList<QBUser>, chatName: String, callback: QBEntityCallback<QBChatDialog>) {
        val dialog = createDialog(users, chatName)
        QBRestChatService.createChatDialog(dialog).performAsync(object : QbEntityCallbackWrapper<QBChatDialog>(callback) {
            override fun onSuccess(t: QBChatDialog, bundle: Bundle?) {
                QbDialogHolder.addDialog(t)
                QbUsersHolder.putUsers(users)
                super.onSuccess(t, bundle)
            }
        })
    }

    fun deleteDialogs(dialogs: Collection<QBChatDialog>, callback: QBEntityCallback<ArrayList<String>>) {
        val dialogsIds = StringifyArrayList<String>()
        for (dialog in dialogs) {
            dialogsIds.add(dialog.dialogId)
        }
        QBRestChatService.deleteDialogs(dialogsIds, false, null).performAsync(callback)
    }

    fun deleteDialog(qbDialog: QBChatDialog, callback: QBEntityCallback<Void>) {
        if (qbDialog.type == QBDialogType.PUBLIC_GROUP) {
            shortToast(R.string.public_group_chat_cannot_be_deleted)
        } else {
            QBRestChatService.deleteDialog(qbDialog.dialogId, false).performAsync(QbEntityCallbackWrapper(callback))
        }
    }

    fun exitFromDialog(qbDialog: QBChatDialog, callback: QBEntityCallback<QBChatDialog>) {
        try {
            leaveChatDialog(qbDialog)
        } catch (e: XMPPException) {
            callback.onError(QBResponseException(e.message))
        } catch (e: SmackException.NotConnectedException) {
            callback.onError(QBResponseException(e.message))
        }
        val currentUser = QBChatService.getInstance().user
        val qbRequestBuilder = QBDialogRequestBuilder()
        qbRequestBuilder.removeUsers(currentUser.id)
        qbDialog.name = buildDialogNameWithoutUser(qbDialog.name, currentUser.fullName)
        QBRestChatService.updateGroupChatDialog(qbDialog, qbRequestBuilder).performAsync(callback)
    }

    fun updateDialogUsers(qbDialog: QBChatDialog,
                          newQbDialogUsersList: List<QBUser>,
                          callback: QBEntityCallback<QBChatDialog>) {
        val addedUsers = getAddedUsers(qbDialog, newQbDialogUsersList)
        val removedUsers = getRemovedUsers(qbDialog, newQbDialogUsersList)

        logDialogUsers(qbDialog)
        logUsers(addedUsers)
        Log.w(TAG, "=======================")
        logUsers(removedUsers)

        val qbRequestBuilder = QBDialogRequestBuilder()
        if (!addedUsers.isEmpty()) {
            qbRequestBuilder.addUsers(*addedUsers.toTypedArray())
        }
        if (!removedUsers.isEmpty()) {
            qbRequestBuilder.removeUsers(*removedUsers.toTypedArray())
        }

        QBRestChatService.updateGroupChatDialog(qbDialog, qbRequestBuilder).performAsync(object : QbEntityCallbackWrapper<QBChatDialog>(callback) {
            override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle?) {
                QbUsersHolder.putUsers(newQbDialogUsersList)
                logDialogUsers(qbChatDialog)
                super.onSuccess(qbChatDialog, bundle)
            }
        })
    }

    fun loadChatHistory(dialog: QBChatDialog, skipPagination: Int, callback: QBEntityCallback<ArrayList<QBChatMessage>>) {
        val messageGetBuilder = QBMessageGetBuilder()
        messageGetBuilder.skip = skipPagination
        messageGetBuilder.limit = CHAT_HISTORY_ITEMS_PER_PAGE
        messageGetBuilder.sortDesc(CHAT_HISTORY_ITEMS_SORT_FIELD)
        messageGetBuilder.markAsRead(false)

        QBRestChatService.getDialogMessages(dialog, messageGetBuilder).performAsync(object : QbEntityCallbackWrapper<ArrayList<QBChatMessage>>(callback) {
            override fun onSuccess(t: ArrayList<QBChatMessage>, bundle: Bundle?) {
                val userIds = HashSet<Int>()
                for (message in t) {
                    userIds.add(message.senderId)
                }
                if (userIds.isNotEmpty()) {
                    getUsersFromMessages(t, userIds, callback)
                } else {
                    callback.onSuccess(t, bundle)
                }
                // Not calling super.onSuccess() because
                // we're want to load chat userList before triggering the callback
            }
        })
    }

    fun getDialogs(requestBuilder: QBRequestGetBuilder, callback: QBEntityCallback<ArrayList<QBChatDialog>>) {

        QBRestChatService.getChatDialogs(null, requestBuilder).performAsync(
                object : QbEntityCallbackWrapper<ArrayList<QBChatDialog>>(callback) {
                    override fun onSuccess(dialogs: ArrayList<QBChatDialog>, bundle: Bundle?) {
                        getUsersFromDialogs(dialogs, callback)
                        // Not calling callback.onSuccess(...) because
                        // we want to load chat userList before triggering callback
                    }
                })
    }

    fun getDialogById(dialogId: String, callback: QBEntityCallback<QBChatDialog>) {
        QBRestChatService.getChatDialogById(dialogId).performAsync(callback)
    }

    fun getUsersFromDialog(dialog: QBChatDialog, callback: QBEntityCallback<ArrayList<QBUser>>) {
        val userIds = dialog.occupants
        val users = ArrayList<QBUser>(userIds.size)
        for (id in userIds) {
            val user = QbUsersHolder.getUserById(id)
            user?.let {
                users.add(it)
            }
        }

        // If we already have all userList in memory
        // there is no need to make REST requests to QB
        if (userIds.size == users.size) {
            callback.onSuccess(users, null)
            return
        }

        val requestBuilder = QBPagedRequestBuilder(USERS_PER_PAGE, 1)
        usersLoadedFromDialog.clear()
        loadUsersByIDsFromDialog(userIds, requestBuilder, callback)
    }

    private fun loadUsersByIDsFromDialog(userIDs: Collection<Int>, requestBuilder: QBPagedRequestBuilder, callback: QBEntityCallback<ArrayList<QBUser>>) {
        QBUsers.getUsersByIDs(userIDs, requestBuilder).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(qbUsers: ArrayList<QBUser>?, bundle: Bundle?) {
                if (qbUsers != null) {
                    usersLoadedFromDialog.addAll(qbUsers)
                    QbUsersHolder.putUsers(qbUsers)
                    bundle?.let {
                        val totalPages = it.get(TOTAL_PAGES_BUNDLE_PARAM) as Int
                        val currentPage = it.get(CURRENT_PAGE_BUNDLE_PARAM) as Int
                        if (totalPages > currentPage) {
                            requestBuilder.page = currentPage + 1
                            loadUsersByIDsFromDialog(userIDs, requestBuilder, callback)
                        } else {
                            callback.onSuccess(usersLoadedFromDialog, bundle)
                        }
                    }
                }
            }

            override fun onError(e: QBResponseException?) {
                callback.onError(e)
            }
        })
    }

    fun loadFileAsAttachment(file: File, callback: QBEntityCallback<QBAttachment>, progressCallback: QBProgressCallback?) {
        QBContent.uploadFileTask(file, false, null, progressCallback).performAsync(
                object : QbEntityCallbackTwoTypeWrapper<QBFile, QBAttachment>(callback) {
                    override fun onSuccess(qbFile: QBFile, bundle: Bundle?) {
                        var type = "file"
                        if (qbFile.contentType.contains("image", ignoreCase = true)) {
                            type = QBAttachment.IMAGE_TYPE
                        } else if (qbFile.contentType.contains("video", ignoreCase = true)) {
                            type = QBAttachment.VIDEO_TYPE
                        }

                        val attachment = QBAttachment(type)
                        attachment.id = qbFile.uid
                        attachment.size = qbFile.size.toDouble()
                        attachment.name = qbFile.name
                        attachment.contentType = qbFile.contentType
                        callback.onSuccess(attachment, bundle)
                    }
                })
    }

    private fun getUsersFromDialogs(dialogs: ArrayList<QBChatDialog>, callback: QBEntityCallback<ArrayList<QBChatDialog>>) {
        val userIds = HashSet<Int>()
        for (dialog in dialogs) {
            userIds.addAll(dialog.occupants)
            userIds.add(dialog.lastMessageUserId)
        }

        val requestBuilder = QBPagedRequestBuilder(USERS_PER_PAGE, 1)
        usersLoadedFromDialogs.clear()
        loadUsersByIDsFromDialogs(userIds, requestBuilder, object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(qbUsers: ArrayList<QBUser>?, b: Bundle?) {
                callback.onSuccess(dialogs, b)
            }

            override fun onError(e: QBResponseException?) {
                callback.onError(e)
            }
        })
    }

    private fun loadUsersByIDsFromDialogs(userIDs: Collection<Int>, requestBuilder: QBPagedRequestBuilder, callback: QBEntityCallback<ArrayList<QBUser>>) {
        QBUsers.getUsersByIDs(userIDs, requestBuilder).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(qbUsers: ArrayList<QBUser>?, bundle: Bundle?) {
                if (qbUsers != null) {
                    usersLoadedFromDialogs.addAll(qbUsers)
                    QbUsersHolder.putUsers(qbUsers)
                    bundle?.let {
                        val totalPages = it.get(TOTAL_PAGES_BUNDLE_PARAM) as Int
                        val currentPage = it.get(CURRENT_PAGE_BUNDLE_PARAM) as Int
                        if (totalPages > currentPage) {
                            requestBuilder.page = currentPage + 1
                            loadUsersByIDsFromDialogs(userIDs, requestBuilder, callback)
                        } else {
                            callback.onSuccess(usersLoadedFromDialogs, bundle)
                        }
                    }
                }
            }

            override fun onError(e: QBResponseException?) {
                callback.onError(e)
            }
        })
    }

    private fun getUsersFromMessages(messages: ArrayList<QBChatMessage>,
                                     userIds: Set<Int>,
                                     callback: QBEntityCallback<ArrayList<QBChatMessage>>) {

        val requestBuilder = QBPagedRequestBuilder(USERS_PER_PAGE, 1)
        usersLoadedFromMessages.clear()
        loadUsersByIDsFromMessages(userIds, requestBuilder, object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(qbUsers: ArrayList<QBUser>?, b: Bundle?) {
                callback.onSuccess(messages, b)
            }

            override fun onError(e: QBResponseException?) {
                callback.onError(e)
            }
        })
    }

    private fun loadUsersByIDsFromMessages(userIDs: Collection<Int>, requestBuilder: QBPagedRequestBuilder, callback: QBEntityCallback<ArrayList<QBUser>>) {
        QBUsers.getUsersByIDs(userIDs, requestBuilder).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(qbUsers: ArrayList<QBUser>?, bundle: Bundle?) {
                if (qbUsers != null) {
                    usersLoadedFromMessages.addAll(qbUsers)
                    QbUsersHolder.putUsers(qbUsers)
                    bundle?.let {
                        val totalPages = it.get(TOTAL_PAGES_BUNDLE_PARAM) as Int
                        val currentPage = it.get(CURRENT_PAGE_BUNDLE_PARAM) as Int
                        if (totalPages > currentPage) {
                            requestBuilder.page = currentPage + 1
                            loadUsersByIDsFromMessages(userIDs, requestBuilder, callback)
                        } else {
                            callback.onSuccess(usersLoadedFromMessages, bundle)
                        }
                    }
                }
            }

            override fun onError(e: QBResponseException?) {
                callback.onError(e)
            }
        })
    }

    fun getUsersFromMessage(message: QBChatMessage, callback: QBEntityCallback<ArrayList<QBUser>>) {
        val userIds = ArrayList<Int>()
        val usersDelivered = message.deliveredIds
        val usersRead = message.readIds

        for (id in usersDelivered) {
            userIds.add(id)
        }
        for (id in usersRead) {
            userIds.add(id)
        }

        val requestBuilder = QBPagedRequestBuilder(USERS_PER_PAGE, 1)
        usersLoadedFromMessage.clear()
        loadUsersByIDsFromMessage(userIds, requestBuilder, callback)
    }

    private fun loadUsersByIDsFromMessage(userIDs: Collection<Int>, requestBuilder: QBPagedRequestBuilder, callback: QBEntityCallback<ArrayList<QBUser>>) {
        QBUsers.getUsersByIDs(userIDs, requestBuilder).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(qbUsers: ArrayList<QBUser>?, bundle: Bundle?) {
                if (qbUsers != null) {
                    usersLoadedFromMessage.addAll(qbUsers)
                    QbUsersHolder.putUsers(qbUsers)
                    bundle?.let {
                        val totalPages = it.get(TOTAL_PAGES_BUNDLE_PARAM) as Int
                        val currentPage = it.get(CURRENT_PAGE_BUNDLE_PARAM) as Int
                        if (totalPages > currentPage) {
                            requestBuilder.page = currentPage + 1
                            loadUsersByIDsFromMessages(userIDs, requestBuilder, callback)
                        } else {
                            callback.onSuccess(usersLoadedFromMessage, bundle)
                        }
                    }
                }
            }

            override fun onError(e: QBResponseException?) {
                callback.onError(e)
            }
        })
    }
}