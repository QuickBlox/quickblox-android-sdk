package com.quickblox.sample.chat.kotlin.ui.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.utils.getColorCircleDrawable
import com.quickblox.sample.chat.kotlin.utils.getGreyCircleDrawable
import com.quickblox.sample.chat.kotlin.utils.qb.getDialogName

private const val MAX_MESSAGES_TEXT = "99+"
private const val MAX_MESSAGES = 99

class DialogsAdapter(var context: Context, var dialogs: List<QBChatDialog>) : BaseAdapter() {

    private var _selectedItems: ArrayList<QBChatDialog> = ArrayList()
    val selectedItems: ArrayList<QBChatDialog>
        get() = _selectedItems

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var modifiedView = convertView
        val holder: ViewHolder
        if (modifiedView == null) {
            modifiedView = LayoutInflater.from(context).inflate(R.layout.list_item_dialog, parent, false)
            holder = ViewHolder()
            holder.rootLayout = modifiedView.findViewById(R.id.root)
            holder.nameTextView = modifiedView.findViewById(R.id.text_dialog_name)
            holder.lastMessageTextView = modifiedView.findViewById(R.id.text_dialog_last_message)
            holder.dialogImageView = modifiedView.findViewById(R.id.image_dialog_icon)
            holder.unreadCounterTextView = modifiedView.findViewById(R.id.text_dialog_unread_count)
            modifiedView.tag = holder
        } else {
            holder = modifiedView.tag as ViewHolder
        }

        val dialog = getItem(position)
        if (dialog.type == QBDialogType.GROUP) {
            holder.dialogImageView.setBackgroundDrawable(getGreyCircleDrawable())
            holder.dialogImageView.setImageResource(R.drawable.ic_chat_group)
        } else {
            holder.dialogImageView.setBackgroundDrawable(getColorCircleDrawable(position))
            holder.dialogImageView.setImageDrawable(null)
        }

        holder.nameTextView.text = getDialogName(dialog)
        holder.lastMessageTextView.text = prepareTextLastMessage(dialog)

        val unreadMessagesCount = getUnreadMsgCount(dialog)
        if (unreadMessagesCount == 0) {
            holder.unreadCounterTextView.visibility = View.GONE
        } else {
            holder.unreadCounterTextView.visibility = View.VISIBLE
            val messageCount = if (unreadMessagesCount > MAX_MESSAGES) {
                MAX_MESSAGES_TEXT
            } else {
                unreadMessagesCount.toString()
            }
            holder.unreadCounterTextView.text = messageCount
        }

        val backgroundColor = if (isItemSelected(position)) {
            context.resources.getColor(R.color.selected_list_item_color)
        } else {
            context.resources.getColor(android.R.color.transparent)
        }
        holder.rootLayout.setBackgroundColor(backgroundColor)

        return modifiedView!!
    }

    override fun getItem(position: Int): QBChatDialog {
        return dialogs[position]
    }

    override fun getItemId(id: Int): Long {
        return id.toLong()
    }

    override fun getCount(): Int {
        return dialogs.size
    }

    private fun isItemSelected(position: Int): Boolean {
        return !_selectedItems.isEmpty() && _selectedItems.contains(getItem(position))
    }

    private fun getUnreadMsgCount(chatDialog: QBChatDialog): Int {
        val unreadMessageCount = chatDialog.unreadMessageCount
        return unreadMessageCount ?: 0
    }

    private fun isLastMessageAttachment(dialog: QBChatDialog): Boolean {
        val lastMessage = dialog.lastMessage
        val lastMessageSenderId = dialog.lastMessageUserId
        return TextUtils.isEmpty(lastMessage) && lastMessageSenderId != null
    }

    private fun prepareTextLastMessage(chatDialog: QBChatDialog): String {
        var lastMessage = ""
        if (isLastMessageAttachment(chatDialog)) {
            lastMessage = context.getString(R.string.chat_attachment)
        } else {
            chatDialog.lastMessage?.let {
                lastMessage = it
            }
        }
        return lastMessage
    }

    fun clearSelection() {
        _selectedItems.clear()
        notifyDataSetChanged()
    }

    fun updateList(newData: List<QBChatDialog>) {
        dialogs = newData
        notifyDataSetChanged()
    }

    fun selectItem(item: QBChatDialog) {
        if (_selectedItems.contains(item)) {
            return
        }
        _selectedItems.add(item)
        notifyDataSetChanged()
    }

    fun toggleSelection(item: QBChatDialog) {
        if (_selectedItems.contains(item)) {
            _selectedItems.remove(item)
        } else {
            _selectedItems.add(item)
        }
        notifyDataSetChanged()
    }

    private class ViewHolder {
        lateinit var rootLayout: ViewGroup
        lateinit var dialogImageView: ImageView
        lateinit var nameTextView: TextView
        lateinit var lastMessageTextView: TextView
        lateinit var unreadCounterTextView: TextView
    }
}