package com.quickblox.sample.chat.kotlin.utils.qb

import android.text.TextUtils
import android.util.Log
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import com.quickblox.chat.utils.DialogUtils
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.users.model.QBUser
import java.util.*
import kotlin.collections.ArrayList

private const val PRIVATE_CHAT_COUNT_USERS = 2
private const val TAG = "QbDialogUtils::class"

fun createDialog(users: MutableList<QBUser>, chatName: String): QBChatDialog {
    if (isPrivateChat(users)) {
        users.remove(ChatHelper.getCurrentUser())
    }
    val dialog = DialogUtils.buildDialog(*users.toTypedArray())
    if (!TextUtils.isEmpty(chatName)) {
        dialog.name = chatName
    }
    return dialog
}

private fun isPrivateChat(users: List<QBUser>): Boolean {
    return users.size == PRIVATE_CHAT_COUNT_USERS
}

fun getAddedUsers(dialog: QBChatDialog, currentUsers: List<QBUser>): List<QBUser> {
    return getAddedUsers(getQbUsersFromQbDialog(dialog), currentUsers)
}

private fun getAddedUsers(previousUsersList: List<QBUser>, currentUsersList: List<QBUser>): List<QBUser> {
    val addedUsers = ArrayList<QBUser>()
    for (currentUser in currentUsersList) {
        var wasInChatBefore = false
        for (previousUser in previousUsersList) {
            if (currentUser.id == previousUser.id) {
                wasInChatBefore = true
                break
            }
        }
        if (!wasInChatBefore) {
            addedUsers.add(currentUser)
        }
    }
    addedUsers.remove(ChatHelper.getCurrentUser())
    return addedUsers
}

fun getRemovedUsers(dialog: QBChatDialog, currentUsers: List<QBUser>): List<QBUser> {
    return getRemovedUsers(getQbUsersFromQbDialog(dialog), currentUsers)
}

private fun getRemovedUsers(previousUsersList: List<QBUser>, currentUsersList: List<QBUser>): List<QBUser> {
    val removedUsers = ArrayList<QBUser>()
    for (previousUser in previousUsersList) {
        var isUserStillPresented = false
        for (currentUser in currentUsersList) {
            if (previousUser.id == currentUser.id) {
                isUserStillPresented = true
                break
            }
        }
        if (!isUserStillPresented) {
            removedUsers.add(previousUser)
        }
    }
    removedUsers.remove(ChatHelper.getCurrentUser())
    return removedUsers
}

fun logDialogUsers(qbDialog: QBChatDialog) {
    Log.v(TAG, "Dialog " + getDialogName(qbDialog))
    logUsersByIds(qbDialog.occupants)
}

fun logUsers(users: List<QBUser>) {
    for (user in users) {
        Log.i(TAG, user.id.toString() + " " + user.fullName)
    }
}

private fun logUsersByIds(usersIds: List<Int>) {
    for (id in usersIds) {
        val user = QbUsersHolder.getUserById(id)
        Log.i(TAG, user?.id.toString() + " " + user?.fullName)
    }
}

fun getDialogName(dialog: QBChatDialog): String {
    var result = ""
    if (dialog.type == QBDialogType.GROUP) {
        result = dialog.name
    } else {
        // It's a private dialog, let's use opponent's name as chat name
        val opponentId = dialog.recipientId
        val user = QbUsersHolder.getUserById(opponentId)

        user?.let {
            result = if (TextUtils.isEmpty(it.fullName)) {
                it.login
            } else {
                it.fullName
            }
        } ?: run {
            result = dialog.name
        }
    }
    return result
}

private fun getQbUsersFromQbDialog(dialog: QBChatDialog): List<QBUser> {
    val previousDialogUsers = ArrayList<QBUser>()
    for (id in dialog.occupants) {
        val user = QbUsersHolder.getUserById(id)
        user?.let {
            previousDialogUsers.add(it)
        }
    }
    return previousDialogUsers
}

fun getOccupantsIdsListFromString(occupantIds: String?): List<Int> {
    val occupantIdsList = ArrayList<Int>()
    occupantIds?.let {
        val occupantIdsArray = occupantIds.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (occupantId in occupantIdsArray) {
            occupantIdsList.add(Integer.valueOf(occupantId.trim()))
        }
    }
    return occupantIdsList
}

fun getOccupantsIdsStringFromList(occupantIdsList: Collection<Int>): String {
    return TextUtils.join(",", occupantIdsList)
}

fun getOccupantsNamesStringFromList(qbUsers: Collection<QBUser>): String {
    val userNameList = ArrayList<String>()
    for (user in qbUsers) {
        if (TextUtils.isEmpty(user.fullName)) {
            userNameList.add(user.login)
        } else {
            userNameList.add(user.fullName)
        }
    }
    return TextUtils.join(", ", userNameList)
}