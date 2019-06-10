package com.quickblox.sample.videochat.conference.kotlin.util

import android.os.Bundle
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import com.quickblox.chat.request.QBDialogRequestBuilder
import com.quickblox.chat.utils.DialogUtils
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.sample.videochat.conference.kotlin.App
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import java.util.*

private val connectionChecker: NetworkConnectionChecker = NetworkConnectionChecker(App.getInstance())

fun signUpNewUser(newQbUser: QBUser, callback: QBEntityCallback<QBUser>) {
    if (isInternetAvailable()) {
        QBUsers.signUp(newQbUser).performAsync(callback)
    } else {
        notifyInternetError(callback)
    }
}

fun signInUser(currentQbUser: QBUser, callback: QBEntityCallback<QBUser>) {
    if (isInternetAvailable()) {
        QBUsers.signIn(currentQbUser).performAsync(callback)
    } else {
        notifyInternetError(callback)
    }
}

fun signOut() {
    QBUsers.signOut().performAsync(null)
}

fun updateUser(qbUser: QBUser, callback: QBEntityCallback<QBUser>) {
    QBUsers.updateUser(qbUser).performAsync(callback)
}

fun deleteCurrentUser(currentQbUserID: Int, callback: QBEntityCallback<Void>) {
    if (isInternetAvailable()) {
        QBUsers.deleteUser(currentQbUserID).performAsync(callback)
    } else {
        notifyInternetError(callback)
    }
}

fun loadDialogs(callback: QBEntityCallback<ArrayList<QBChatDialog>>) {
    val requestBuilder = QBRequestGetBuilder()
    requestBuilder.limit = 100
    if (isInternetAvailable()) {
        QBRestChatService.getChatDialogs(QBDialogType.GROUP, requestBuilder).performAsync(callback)
    } else {
        notifyInternetError(callback)
    }
}

fun loadDialogByID(dialogId: String, callback: QBEntityCallback<QBChatDialog>) {
    if (isInternetAvailable()) {
        QBRestChatService.getChatDialogById(dialogId).performAsync(callback)
    } else {
        notifyInternetError(callback)
    }
}

fun deleteDialogs(dialogs: Collection<QBChatDialog>, callback: QBEntityCallback<ArrayList<String>>) {
    val dialogsIds = StringifyArrayList<String>()
    for (dialog in dialogs) {
        dialogsIds.add(dialog.dialogId)
    }
    if (isInternetAvailable()) {
        QBRestChatService.deleteDialogs(dialogsIds, false, null).performAsync(callback)
    } else {
        notifyInternetError(callback)
    }
}

fun createDialogWithSelectedUsers(users: List<QBUser>, currentUser: QBUser,
                                  callback: QBEntityCallback<QBChatDialog>) {

    QBRestChatService.createChatDialog(createDialog(users, currentUser)).performAsync(object : QBEntityCallback<QBChatDialog> {
        override fun onSuccess(dialog: QBChatDialog, args: Bundle) {
            callback.onSuccess(dialog, args)
        }

        override fun onError(responseException: QBResponseException) {
            callback.onError(responseException)
        }
    })
}

private fun isInternetAvailable(): Boolean {
    return connectionChecker.isConnectedNow()
}

private fun <T> notifyInternetError(callback: QBEntityCallback<T>?) {
    callback?.let {
        val error = App.getInstance().getString(R.string.no_internet_connection)
        val exception = QBResponseException(error)
        it.onError(exception)
    }
}

private fun createDialog(users: List<QBUser>, currentUser: QBUser): QBChatDialog {
    return DialogUtils.buildDialog(DialogUtils.createChatNameFromUserList(*users.toTypedArray()),
            QBDialogType.GROUP, DialogUtils.getUserIds(users))
}

fun loadUsersByTag(tag: String, callback: QBEntityCallback<ArrayList<QBUser>>) {
    val requestBuilder = QBPagedRequestBuilder()
    requestBuilder.perPage = 50
    val tags = LinkedList<String>()
    tags.add(tag)

    QBUsers.getUsersByTags(tags, requestBuilder).performAsync(callback)
}

fun loadUserById(userId: Int, callback: QBEntityCallback<QBUser>) {
    QBUsers.getUser(userId).performAsync(callback)
}

fun updateDialog(dialog: QBChatDialog, users: Array<QBUser>, callback: QBEntityCallback<QBChatDialog>) {
    val requestBuilder = QBDialogRequestBuilder()
    requestBuilder.addUsers(*users)

    if (isInternetAvailable()) {
        QBRestChatService.updateGroupChatDialog(dialog, requestBuilder).performAsync(callback)
    } else {
        notifyInternetError(callback)
    }
}