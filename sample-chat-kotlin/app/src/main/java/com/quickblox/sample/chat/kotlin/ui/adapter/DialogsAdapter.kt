package com.quickblox.sample.chat.kotlin.ui.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.utils.getColorCircleDrawable
import com.quickblox.sample.chat.kotlin.utils.qb.getDialogName
import java.text.SimpleDateFormat
import java.util.*

private const val MAX_MESSAGES_TEXT = "99+"
private const val MAX_MESSAGES = 99

class DialogsAdapter(var context: Context, var dialogs: List<QBChatDialog>) : BaseAdapter() {

    private var isSelectMode = false
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
            holder.unreadCounterTextView = modifiedView.findViewById(R.id.text_unread_counter)
            holder.lastMessageTimeTextView = modifiedView.findViewById(R.id.text_last_msg_time)
            holder.dialogAvatarTitle = modifiedView.findViewById(R.id.text_dialog_avatar_title)
            holder.checkboxDialog = modifiedView.findViewById(R.id.checkbox_dialogs)
            modifiedView.tag = holder
        } else {
            holder = modifiedView.tag as ViewHolder
        }

        val dialog = getItem(position)

        val nameWithoutSpaces = dialog.name.replace(" ", "")
        val stringTokenizer = StringTokenizer(nameWithoutSpaces, ",")
        val firstLetter = stringTokenizer.nextToken().get(0).toString().toUpperCase()
        var avatarTitle = firstLetter
        if (stringTokenizer.hasMoreTokens()) {
            val secondLetter = stringTokenizer.nextToken().get(0).toString().toUpperCase()
            avatarTitle = firstLetter + secondLetter
        }

        holder.dialogAvatarTitle.text = avatarTitle
        holder.dialogImageView.setImageDrawable(getColorCircleDrawable(position))
        holder.nameTextView.text = getDialogName(dialog)
        holder.lastMessageTextView.text = prepareTextLastMessage(dialog)
        holder.lastMessageTimeTextView.text = getDialogLastMessageTime(dialog.lastMessageDateSent)

        val unreadMessagesCount = getUnreadMsgCount(dialog)
        if (isSelectMode) {
            holder.checkboxDialog.visibility = View.VISIBLE
            holder.lastMessageTimeTextView.visibility = View.INVISIBLE
            holder.unreadCounterTextView.visibility = View.INVISIBLE
        } else if (unreadMessagesCount == 0) {
            holder.checkboxDialog.visibility = View.INVISIBLE
            holder.lastMessageTimeTextView.visibility = View.VISIBLE
            holder.unreadCounterTextView.visibility = View.INVISIBLE
        } else {
            holder.checkboxDialog.visibility = View.INVISIBLE
            holder.lastMessageTimeTextView.visibility = View.VISIBLE
            holder.unreadCounterTextView.visibility = View.VISIBLE
            val messageCount = if (unreadMessagesCount > MAX_MESSAGES) {
                MAX_MESSAGES_TEXT
            } else {
                unreadMessagesCount.toString()
            }
            holder.unreadCounterTextView.text = messageCount
        }

        val backgroundColor: Int
        if (isItemSelected(position)) {
            holder.checkboxDialog.isChecked = true
            backgroundColor = context.resources.getColor(R.color.selected_list_item_color)
        } else {
            holder.checkboxDialog.isChecked = false
            backgroundColor = context.resources.getColor(android.R.color.transparent)
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

    fun prepareToSelect() {
        isSelectMode = true
        notifyDataSetChanged()
    }

    fun clearSelection() {
        isSelectMode = false
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

    private fun getDialogLastMessageTime(seconds: Long): String {
        val timeInMillis = seconds * 1000
        val msgTime = Calendar.getInstance()
        msgTime.timeInMillis = timeInMillis

        if (timeInMillis == 0L) {
            return ""
        }

        val now = Calendar.getInstance()
        val timeFormatToday = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        val dateFormatThisYear = SimpleDateFormat("d MMM", Locale.ENGLISH)
        val lastYearFormat = SimpleDateFormat("dd.MM.yy", Locale.ENGLISH)

        if (now.get(Calendar.DATE) == msgTime.get(Calendar.DATE) && now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR)) {
            return timeFormatToday.format(Date(timeInMillis))
        } else if (now.get(Calendar.DAY_OF_YEAR) - msgTime.get(Calendar.DAY_OF_YEAR) == 1 && now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR)) {
            return context.getString(R.string.yesterday)
        } else if (now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR)) {
            return dateFormatThisYear.format(Date(timeInMillis))
        } else {
            return lastYearFormat.format(Date(timeInMillis))
        }
    }

    private class ViewHolder {
        lateinit var rootLayout: ViewGroup
        lateinit var dialogImageView: ImageView
        lateinit var nameTextView: TextView
        lateinit var lastMessageTextView: TextView
        lateinit var unreadCounterTextView: TextView
        lateinit var lastMessageTimeTextView: TextView
        lateinit var dialogAvatarTitle: TextView
        lateinit var checkboxDialog: CheckBox
    }
}