package com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.chat.model.QBAttachment
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ChatViewModel.Companion.MessageType.Companion.HEADER
import com.quickblox.users.model.QBUser
import java.text.SimpleDateFormat
import java.util.*

private const val TYPE_TEXT_RIGHT = 1
private const val TYPE_TEXT_LEFT = 2
private const val TYPE_ATTACHMENT_RIGHT = 3
private const val TYPE_ATTACHMENT_LEFT = 4
private const val TYPE_NOTIFICATION_CENTER = 5
private const val TYPE_NOTIFICATION_CONFERENCE_RIGHT = 6
private const val TYPE_NOTIFICATION_CONFERENCE_LEFT = 7
private const val TYPE_DATE_HEADER = 8
const val PROPERTY_NOTIFICATION_TYPE = "notification_type"
const val START_CONFERENCE = "4"
const val START_STREAM = "5"

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class ChatAdapter(private val chatMessages: ArrayList<ChatMessage>, private val currentUser: QBUser, private val usersDialog: HashSet<QBUser>,
                  private val chatAdapterListener: ChatAdapterListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    HeaderDecoration.HeaderInterface {
    private var paginationListener: PaginationListener? = null
    private var previousGetCount = 0

    init {
        setHasStableIds(true)
    }

    fun setPaginationHistoryListener(paginationListener: PaginationListener?) {
        this.paginationListener = paginationListener
    }

    override fun getItemId(position: Int): Long {
        return chatMessages[position].hashCode().toLong()
    }

    private fun downloadMore(position: Int) {
        if (position == 0) {
            if (itemCount != previousGetCount) {
                paginationListener?.onNextPage()
                previousGetCount = itemCount
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val chatMessage = chatMessages[position]
        var itemViewType: Int = TYPE_DATE_HEADER

        if (chatMessage.type == HEADER) {
            return itemViewType
        }

        if (chatMessage.qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE) != null) {
            val notificationType = chatMessage.qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE) as String
            val conference = notificationType == START_CONFERENCE
            val stream = notificationType == START_STREAM

            itemViewType = if (conference || stream) {
                if (isIncoming(chatMessage)) {
                    TYPE_NOTIFICATION_CONFERENCE_LEFT
                } else {
                    TYPE_NOTIFICATION_CONFERENCE_RIGHT
                }
            } else {
                TYPE_NOTIFICATION_CENTER
            }
            return itemViewType
        }
        // TODO: 7/1/21 Need to refactor
        if (chatMessage.qbChatMessage.attachments != null && chatMessage.qbChatMessage.attachments.isNotEmpty()) {
            val attachment: QBAttachment? = getAttachment(position)
            if (attachment != null) {
                val photo = QBAttachment.PHOTO_TYPE.equals(attachment.type, ignoreCase = true)
                val image = QBAttachment.IMAGE_TYPE.equals(attachment.type, ignoreCase = true)
                val video = QBAttachment.VIDEO_TYPE.equals(attachment.type, ignoreCase = true)
                val audio = QBAttachment.AUDIO_TYPE.equals(attachment.type, ignoreCase = true)
                val file = attachment.type == "file" || attachment.type.contains("file") || attachment.type == ""
                if (photo || image || video || audio || file) {
                    itemViewType = if (isIncoming(chatMessage)) {
                        TYPE_ATTACHMENT_LEFT
                    } else {
                        TYPE_ATTACHMENT_RIGHT
                    }
                }
            }
            return itemViewType
        }
        itemViewType = if (isIncoming(chatMessage)) {
            TYPE_TEXT_LEFT
        } else {
            TYPE_TEXT_RIGHT
        }
        return itemViewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_NOTIFICATION_CENTER -> NotificationViewHolder.newInstance(parent)
            TYPE_TEXT_RIGHT -> TextRightViewHolder.newInstance(parent)
            TYPE_TEXT_LEFT -> TextLeftViewHolder.newInstance(parent)
            TYPE_ATTACHMENT_RIGHT -> AttachRightViewHolder.newInstance(parent, AttachmentClickListenerImpl())
            TYPE_ATTACHMENT_LEFT -> AttachLeftViewHolder.newInstance(parent, AttachmentClickListenerImpl())
            TYPE_NOTIFICATION_CONFERENCE_RIGHT -> ConferenceRightViewHolder.newInstance(parent, JoinConferenceClickListenerImpl())
            TYPE_NOTIFICATION_CONFERENCE_LEFT -> ConferenceLeftViewHolder.newInstance(parent, JoinConferenceClickListenerImpl())
            TYPE_DATE_HEADER -> HeaderViewHolder.newInstance(parent)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        downloadMore(position)
        when (holder) {
            is HeaderViewHolder -> {
                chatMessages[position].let { holder.bind(it) }
            }
            is NotificationViewHolder -> {
                chatMessages[position].let { holder.bind(it) }
            }
            is TextRightViewHolder -> {
                chatMessages[position].let { holder.bind(it) }
            }
            is TextLeftViewHolder -> {
                for (user in usersDialog) {
                    if (chatMessages[position].qbChatMessage.senderId == user.id) {
                        chatMessages[position].let { holder.bind(it, user) }
                        break
                    }
                }
            }
            is AttachRightViewHolder -> {
                chatMessages[position].let { holder.bind(it) }
            }
            is AttachLeftViewHolder -> {
                for (user in usersDialog) {
                    if (chatMessages[position].qbChatMessage.senderId == user.id) {
                        chatMessages[position].let { holder.bind(it, user) }
                        break
                    }
                }
            }
            is ConferenceRightViewHolder -> {
                chatMessages[position].let { holder.bind(it) }
            }
            is ConferenceLeftViewHolder -> {
                for (user in usersDialog) {
                    if (chatMessages[position].qbChatMessage.senderId == user.id) {
                        chatMessages[position].let { holder.bind(it, user) }
                        break
                    }
                }
            }
        }
        if (isIncoming(chatMessages[position]) && !isReadByCurrentUser(chatMessages[position])) {
            chatAdapterListener.readMessage(chatMessages[position].qbChatMessage)
        }
    }

    private fun isReadByCurrentUser(chatMessage: ChatMessage): Boolean {
        return chatMessage.qbChatMessage.readIds?.contains(currentUser.id) ?: false
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    private fun isIncoming(chatMessage: ChatMessage): Boolean {
        return chatMessage.qbChatMessage.senderId != null && chatMessage.qbChatMessage.senderId != currentUser.id
    }

    private fun getAttachment(position: Int): QBAttachment? {
        val chatMessage = chatMessages[position]
        return chatMessage.qbChatMessage.attachments.toList()[0]
    }

    // TODO: 6/17/21 Refactor
    override fun getHeaderPosition(itemPosition: Int): Int {
        var position = itemPosition
        var headerPosition = 0
        do {
            if (isHeader(position)) {
                headerPosition = position
                break
            }
            position -= 1
        } while (position > 0)
        return headerPosition
    }

    override fun getHeaderLayout(headerPosition: Int?): Int {
        return R.layout.item_header_message
    }

    override fun bind(header: View?, headerPosition: Int) {
        val tvDate = header?.findViewById<TextView>(R.id.tvDate)

        val chatMessage = chatMessages[headerPosition].qbChatMessage
        val date: String
        val timeInMillis: Long = chatMessage.dateSent.times(1000)
        val msgTime = Calendar.getInstance()
        msgTime.timeInMillis = timeInMillis

        val now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("d MMM", Locale.ENGLISH)
        val lastYearFormat = SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH)

        val sameYear = now[Calendar.YEAR] == msgTime[Calendar.YEAR]
        val sameMonth = now[Calendar.MONTH] == msgTime[Calendar.MONTH]
        val sameDay = now[Calendar.DATE] == msgTime[Calendar.DATE]
        val lastDay = now[Calendar.DAY_OF_YEAR] - msgTime[Calendar.DAY_OF_YEAR] == 1

        date = if (sameDay && sameMonth && sameYear) {
            tvDate?.context?.getString(R.string.today).toString()
        } else if (lastDay and sameYear) {
            tvDate?.context?.getString(R.string.yesterday).toString()
        } else if (sameYear) {
            dateFormat.format(Date(timeInMillis))
        } else {
            lastYearFormat.format(Date(timeInMillis))
        }
        tvDate?.text = date
    }

    override fun isHeader(itemPosition: Int): Boolean {
        if (itemPosition < 0) {
            return false
        }
        var chatMessage: ChatMessage? = null
        if (chatMessages.isNotEmpty() && itemPosition < chatMessages.size) {
            chatMessage = chatMessages[itemPosition]
        }
        return chatMessage?.type == 1
    }

    interface ChatAdapterListener {
        fun readMessage(qbChatMessage: QBChatMessage?)
        fun onClickAttachment(url: String)
        fun onClickJoin(chatMessage: ChatMessage)
    }

    private inner class AttachmentClickListenerImpl : AttachmentClickListener {
        override fun onClick(url: String) {
            chatAdapterListener.onClickAttachment(url)
        }
    }

    private inner class JoinConferenceClickListenerImpl : JoinConferenceClickListener {
        override fun clickJoin(qbChatMessage: ChatMessage) {
            chatAdapterListener.onClickJoin(qbChatMessage)
        }
    }

    interface PaginationListener {
        fun onNextPage()
    }
}