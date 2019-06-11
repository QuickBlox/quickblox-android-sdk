package com.quickblox.sample.chat.kotlin.utils.qb

import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.users.model.QBUser
import java.util.*

private const val DEFAULT_MESSAGE_COUNT = 1

object QbDialogHolder {

    private val _dialogsMap: MutableMap<String, QBChatDialog> = TreeMap()
    val dialogsMap: Map<String, QBChatDialog>
        get() = getSortedMap(_dialogsMap)

    fun getChatDialogById(dialogId: String): QBChatDialog? {
        return _dialogsMap[dialogId]
    }

    fun clear() {
        _dialogsMap.clear()
    }

    fun addDialog(dialog: QBChatDialog?) {
        dialog?.let {
            _dialogsMap[it.dialogId] = it
        }
    }

    fun addDialogs(dialogs: List<QBChatDialog>) {
        for (dialog in dialogs) {
            addDialog(dialog)
        }
    }

    fun deleteDialogs(dialogsIds: ArrayList<String>) {
        for (dialogId in dialogsIds) {
            deleteDialog(dialogId)
        }
    }

    fun deleteDialog(chatDialog: QBChatDialog) {
        _dialogsMap.remove(chatDialog.dialogId)
    }

    private fun deleteDialog(dialogId: String) {
        _dialogsMap.remove(dialogId)
    }

    fun hasDialogWithId(dialogId: String): Boolean {
        return _dialogsMap.containsKey(dialogId)
    }

    fun hasPrivateDialogWithUser(user: QBUser): Boolean {
        return getPrivateDialogWithUser(user) != null
    }

    fun getPrivateDialogWithUser(user: QBUser): QBChatDialog? {
        var privateDialog: QBChatDialog? = null
        for (chatDialog in _dialogsMap.values) {
            if (QBDialogType.PRIVATE == chatDialog.type && chatDialog.occupants.contains(user.id)) {
                privateDialog = chatDialog
            }
        }
        return privateDialog
    }

    private fun getSortedMap(unsortedMap: Map<String, QBChatDialog>): Map<String, QBChatDialog> {
        val sortedMap = TreeMap<String, QBChatDialog>(LastMessageDateSentComparator(unsortedMap))
        sortedMap.putAll(unsortedMap)
        return sortedMap
    }

    fun updateDialog(dialogId: String, qbChatMessage: QBChatMessage) {
        val updatedDialog = getChatDialogById(dialogId)
        updatedDialog?.let {
            it.lastMessage = qbChatMessage.body
            it.lastMessageDateSent = qbChatMessage.dateSent
            val messageCount = if (it.unreadMessageCount != null) {
                updatedDialog.unreadMessageCount + 1
            } else {
                DEFAULT_MESSAGE_COUNT
            }
            it.unreadMessageCount = messageCount
            it.lastMessageUserId = qbChatMessage.senderId
            _dialogsMap[updatedDialog.dialogId] = updatedDialog
        }
    }

    internal class LastMessageDateSentComparator(var map: Map<String, QBChatDialog>) : Comparator<String> {
        override fun compare(keyA: String, keyB: String): Int {
            val valueA = map[keyA]?.lastMessageDateSent ?: return -1
            val valueB = map[keyB]?.lastMessageDateSent ?: return -1
            val result = if (valueB < valueA) {
                -1
            } else {
                1
            }
            return result
        }
    }
}