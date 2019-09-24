package com.quickblox.sample.chat.kotlin.ui.adapter

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quickblox.chat.model.QBAttachment
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.content.model.QBFile
import com.quickblox.core.helper.CollectionsUtil
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.managers.PROPERTY_NOTIFICATION_TYPE
import com.quickblox.sample.chat.kotlin.ui.adapter.listeners.AttachClickListener
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.sample.chat.kotlin.utils.getDateAsHeaderId
import com.quickblox.sample.chat.kotlin.utils.getRandomTextColorById
import com.quickblox.sample.chat.kotlin.utils.qb.PaginationHistoryListener
import com.quickblox.sample.chat.kotlin.utils.qb.QbUsersHolder
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import java.text.SimpleDateFormat
import java.util.*

private const val CUSTOM_VIEW_TYPE = -1
private const val TYPE_TEXT_RIGHT = 1
private const val TYPE_TEXT_LEFT = 2
private const val TYPE_ATTACH_RIGHT = 3
private const val TYPE_ATTACH_LEFT = 4
private const val TYPE_NOTIFICATION_CENTER = 5

class ChatAdapter(protected var context: Context,
                  private val chatDialog: QBChatDialog,
                  private val chatMessages: MutableList<QBChatMessage>) :
        RecyclerView.Adapter<ChatAdapter.MessageViewHolder>(),
        StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private val TAG = ChatAdapter::class.java.simpleName

    private var paginationListener: PaginationHistoryListener? = null
    private var previousGetCount = 0
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var attachImageClickListener: AttachClickListener? = null

    private val containerLayoutRes = object : SparseIntArray() {
        init {
            put(TYPE_TEXT_RIGHT, R.layout.list_item_text_right)
            put(TYPE_TEXT_LEFT, R.layout.list_item_text_left)
            put(TYPE_ATTACH_RIGHT, R.layout.list_item_attach_right)
            put(TYPE_ATTACH_LEFT, R.layout.list_item_attach_left)
            put(TYPE_NOTIFICATION_CENTER, R.layout.list_item_notif_center)
        }
    }

    fun updateStatusDelivered(messageID: String, userId: Int) {
        for (position in chatMessages.indices) {
            val message = chatMessages[position]
            if (message.id == messageID) {
                val deliveredIds = ArrayList<Int>()
                if (message.deliveredIds != null) {
                    deliveredIds.addAll(message.deliveredIds)
                }
                deliveredIds.add(userId)
                message.deliveredIds = deliveredIds
                notifyItemChanged(position)
            }
        }
    }

    fun updateStatusRead(messageID: String, userId: Int) {
        for (position in chatMessages.indices) {
            val message = chatMessages[position]
            if (message.id == messageID) {
                val readIds = ArrayList<Int>()
                if (message.readIds != null) {
                    readIds.addAll(message.readIds)
                }
                readIds.add(userId)
                message.readIds = readIds
                notifyItemChanged(position)
            }
        }
    }

    fun setAttachImageClickListener(clickListener: AttachClickListener) {
        attachImageClickListener = clickListener
    }

    fun removeAttachImageClickListener() {
        attachImageClickListener = null
    }

    fun addMessages(items: List<QBChatMessage>) {
        chatMessages.addAll(0, items)
        notifyItemRangeInserted(0, items.size)
    }

    fun setMessages(items: List<QBChatMessage>) {
        chatMessages.clear()
        chatMessages.addAll(items)
        notifyDataSetChanged()
    }

    fun addMessage(item: QBChatMessage) {
        this.chatMessages.add(item)
        this.notifyItemInserted(chatMessages.size - 1)
    }

    fun getMessages(): List<QBChatMessage> {
        return chatMessages
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        when (viewType) {
            TYPE_NOTIFICATION_CENTER -> {
                return NotificationHolder(inflater.inflate(containerLayoutRes.get(viewType), parent,
                        false), R.id.msg_text_message, R.id.msg_text_time_message)
            }
            TYPE_TEXT_RIGHT -> {
                return TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent,
                        false), R.id.msg_text_message, R.id.msg_text_time_message,
                        R.id.msg_link_preview, R.id.msg_text_status_message)
            }
            TYPE_TEXT_LEFT -> {
                return TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent,
                        false), R.id.msg_text_message, R.id.msg_text_time_message,
                        R.id.msg_link_preview)
            }
            TYPE_ATTACH_RIGHT -> {
                return ImageAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent,
                        false), R.id.msg_image_attach, R.id.msg_progressbar_attach,
                        R.id.msg_text_time_attach, R.id.msg_attach_status_message)
            }
            TYPE_ATTACH_LEFT -> {
                return ImageAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent,
                        false), R.id.msg_image_attach, R.id.msg_progressbar_attach,
                        R.id.msg_text_time_attach)
            }
            else -> {
                Log.d(TAG, "onCreateViewHolder case default")
                // resource must be set manually by creating custom adapter
                return TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent,
                        false), R.id.msg_text_message, R.id.msg_text_time_message,
                        R.id.msg_link_preview, R.id.msg_text_status_message)
            }
        }
    }

    override fun onViewRecycled(holder: MessageViewHolder) {
        if (holder.itemViewType == TYPE_TEXT_LEFT || holder.itemViewType == TYPE_TEXT_RIGHT) {
            val textMessageHolder = holder as TextMessageHolder
            textMessageHolder.linkPreviewLayout.tag = null
        }

        //abort loading avatar before setting new avatar to view
        if (containerLayoutRes.get(holder.itemViewType) != 0 && holder.avatar != null) {
            Glide.clear(holder.avatar)
        }

        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        downloadMore(position)
        val chatMessage = getItem(position)
        if (isIncoming(chatMessage) && !isReadByCurrentUser(chatMessage)) {
            readMessage(chatMessage)
        }
        Log.d(TAG, "onBindViewHolder on position $position")
        when (getItemViewType(position)) {
            TYPE_NOTIFICATION_CENTER -> {
                onBindViewNotificationHolder(holder as NotificationHolder, chatMessage, position)
            }
            TYPE_TEXT_RIGHT -> {
                onBindViewMsgRightHolder(holder as TextMessageHolder, chatMessage)
            }
            TYPE_TEXT_LEFT -> {
                onBindViewMsgLeftHolder(holder as TextMessageHolder, chatMessage)
            }
            TYPE_ATTACH_RIGHT -> {
                onBindViewAttachRightHolder(holder as ImageAttachHolder, chatMessage, position)
            }
            TYPE_ATTACH_LEFT -> {
                onBindViewAttachLeftHolder(holder as ImageAttachHolder, chatMessage, position)
            }
            else -> {
                Log.d(TAG, "onBindViewHolder TYPE_ATTACHMENT_CUSTOM")
            }
        }
    }

    private fun onBindViewNotificationHolder(holder: NotificationHolder, chatMessage: QBChatMessage, position: Int) {
        holder.messageTextView.text = chatMessage.body
        holder.messageTimeTextView.text = getTime(chatMessage.dateSent)
    }

    private fun onBindViewMsgRightHolder(holder: TextMessageHolder, chatMessage: QBChatMessage) {
        fillTextMessageHolder(holder, chatMessage, false)
    }

    private fun onBindViewMsgLeftHolder(holder: TextMessageHolder, chatMessage: QBChatMessage) {
        holder.messageTimeTextView.visibility = View.GONE
        val customMessageTimeTextView = holder.itemView.findViewById<TextView>(R.id.custom_msg_text_time_message)
        customMessageTimeTextView.text = getTime(chatMessage.dateSent)

        setOpponentsName(holder, chatMessage, false)
        fillTextMessageHolder(holder, chatMessage, true)
    }

    private fun fillTextMessageHolder(holder: TextMessageHolder, chatMessage: QBChatMessage, isLeftMessage: Boolean) {
        holder.linkPreviewLayout.visibility = View.GONE
        holder.messageTextView.text = chatMessage.body
        holder.messageTimeTextView.text = getTime(chatMessage.dateSent)

        val urlsList = extractUrls(chatMessage.body)
        if (urlsList.isEmpty()) {
            holder.messageTextView.maxWidth = context.resources.displayMetrics.widthPixels
        } else {
            holder.messageTextView.maxWidth = context.resources.getDimension(R.dimen.link_preview_width).toInt()
            holder.linkPreviewLayout.tag = chatMessage.id
        }

        if (!isLeftMessage) {
            val read = isRead(chatMessage)
            val delivered = isDelivered(chatMessage)
            if (read) {
                holder.messageStatusTextView?.setText(R.string.statuses_read)
            } else if (delivered) {
                holder.messageStatusTextView?.setText(R.string.statuses_delivered)
            } else {
                holder.messageStatusTextView?.setText(R.string.statuses_sent)
            }
        }
    }

    private fun extractUrls(text: String): List<String> {
        val containedUrls = ArrayList<String>()
        val pattern = Patterns.WEB_URL
        val urlMatcher = pattern.matcher(text)
        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)))
        }
        return containedUrls
    }

    private fun onBindViewAttachRightHolder(holder: ImageAttachHolder, chatMessage: QBChatMessage, position: Int) {
        fillAttachHolder(holder, chatMessage, position, false)
    }

    private fun onBindViewAttachLeftHolder(holder: ImageAttachHolder, chatMessage: QBChatMessage, position: Int) {
        setOpponentsName(holder, chatMessage, true)
        fillAttachHolder(holder, chatMessage, position, true)
    }

    private fun setOpponentsName(holder: MessageViewHolder, chatMessage: QBChatMessage, isAttachment: Boolean) {
        val viewId = if (isAttachment) R.id.opponent_name_attach_view else R.id.opponent_name_text_view
        val opponentNameTextView = holder.itemView.findViewById<TextView>(viewId)
        opponentNameTextView.setTextColor(getRandomTextColorById(chatMessage.senderId))
        opponentNameTextView.text = getSenderName(chatMessage)
    }

    private fun fillAttachHolder(holder: ImageAttachHolder, chatMessage: QBChatMessage, position: Int, isLeftMessage: Boolean) {
        setDateSentAttach(holder, chatMessage)
        displayAttachment(holder, position)
        setItemAttachClickListener(getAttachListenerByType(position), holder, getAttachment(position), position)

        if (!isLeftMessage) {
            val read = isRead(chatMessage)
            val delivered = isDelivered(chatMessage)
            if (read) {
                holder.attachStatusTextView?.setText(R.string.statuses_read)
            } else if (delivered) {
                holder.attachStatusTextView?.setText(R.string.statuses_delivered)
            } else {
                holder.attachStatusTextView?.setText(R.string.statuses_sent)
            }
        }
    }

    private fun setDateSentAttach(holder: ImageAttachHolder, chatMessage: QBChatMessage) {
        holder.attachTextTime.text = getTime(chatMessage.dateSent)
    }

    private fun displayAttachment(holder: MessageViewHolder, position: Int) {
        val attachment = getAttachment(position)
        val photo = QBAttachment.PHOTO_TYPE.equals(attachment.type, ignoreCase = true)
        val image = QBAttachment.IMAGE_TYPE.equals(attachment.type, ignoreCase = true)
        if (photo || image) {
            showPhotoAttach(holder, position)
        }
    }

    private fun showPhotoAttach(holder: MessageViewHolder, position: Int) {
        val imageUrl = getImageUrl(position)
        showImageByURL(holder, imageUrl)
    }

    private fun setItemAttachClickListener(listener: AttachClickListener?,
                                           holder: MessageViewHolder,
                                           qbAttachment: QBAttachment,
                                           position: Int) {
        listener?.let {
            holder.bubbleFrame?.setOnClickListener(ItemClickListenerFilter(it, qbAttachment, position))
        }
    }

    private fun getAttachListenerByType(position: Int): AttachClickListener? {
        val attachment = getAttachment(position)
        if (QBAttachment.PHOTO_TYPE.equals(attachment.type, ignoreCase = true)
                || QBAttachment.IMAGE_TYPE.equals(attachment.type, ignoreCase = true)) {
            return attachImageClickListener
        } else {
            return null
        }
    }

    private fun getSenderName(chatMessage: QBChatMessage): String {
        val sender = QbUsersHolder.getUserById(chatMessage.senderId!!)
        var fullName = ""
        if (!TextUtils.isEmpty(sender?.fullName)) {
            fullName = sender!!.fullName
        }
        return fullName
    }

    private fun readMessage(chatMessage: QBChatMessage) {
        try {
            chatDialog.readMessage(chatMessage)
        } catch (e: XMPPException) {
            Log.w(TAG, e)
        } catch (e: SmackException.NotConnectedException) {
            Log.w(TAG, e)
        }
    }

    private fun isReadByCurrentUser(chatMessage: QBChatMessage): Boolean {
        val currentUserId = ChatHelper.getCurrentUser().id
        return !CollectionsUtil.isEmpty(chatMessage.readIds) && chatMessage.readIds.contains(currentUserId)
    }

    private fun isRead(chatMessage: QBChatMessage): Boolean {
        var read = false
        val recipientId = chatMessage.recipientId
        val currentUserId = ChatHelper.getCurrentUser().id
        val readIds = chatMessage.readIds ?: return false
        if (recipientId != null && recipientId != currentUserId && readIds.contains(recipientId)) {
            read = true
        } else if (readIds.size == 1 && readIds.contains(currentUserId)) {
            read = false
        } else if (readIds.isNotEmpty()) {
            read = true
        }
        return read
    }

    private fun isDelivered(chatMessage: QBChatMessage): Boolean {
        var delivered = false
        val recipientId = chatMessage.recipientId
        val currentUserId = ChatHelper.getCurrentUser().id
        val deliveredIds = chatMessage.deliveredIds ?: return false
        if (recipientId != null && recipientId != currentUserId && deliveredIds.contains(recipientId)) {
            delivered = true
        } else if (deliveredIds.size == 1 && deliveredIds.contains(currentUserId)) {
            delivered = false
        } else if (deliveredIds.isNotEmpty()) {
            delivered = true
        }
        return delivered
    }

    fun setPaginationHistoryListener(paginationListener: PaginationHistoryListener) {
        this.paginationListener = paginationListener
    }

    private fun downloadMore(position: Int) {
        if (position == 0) {
            if (itemCount != previousGetCount) {
                paginationListener?.downloadMore()
                previousGetCount = itemCount
            }
        }
    }

    override fun getHeaderId(position: Int): Long {
        val chatMessage = getItem(position)
        return getDateAsHeaderId(chatMessage.dateSent * 1000)
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = inflater.inflate(R.layout.view_chat_message_header, parent, false)
        return object : RecyclerView.ViewHolder(view) {

        }
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val view = holder.itemView
        val dateTextView = view.findViewById<TextView>(R.id.header_date_textview)

        val chatMessage = getItem(position)
        dateTextView.text = getDate(chatMessage.dateSent)

        val layoutParams = dateTextView.layoutParams as LinearLayout.LayoutParams
        layoutParams.topMargin = 0
        dateTextView.layoutParams = layoutParams
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    private fun getItem(position: Int): QBChatMessage {
        return chatMessages[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        val chatMessage = getItem(position)
        var itemViewType = CUSTOM_VIEW_TYPE

        if (chatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE) != null) {
            itemViewType = TYPE_NOTIFICATION_CENTER
        } else if (chatMessage.attachments?.isNotEmpty() == true) {
            val attachment = getAttachment(position)
            val photo = QBAttachment.PHOTO_TYPE.equals(attachment.type, ignoreCase = true)
            val image = QBAttachment.IMAGE_TYPE.equals(attachment.type, ignoreCase = true)
            if (photo || image) {
                if (isIncoming(chatMessage)) {
                    itemViewType = TYPE_ATTACH_LEFT
                } else {
                    itemViewType = TYPE_ATTACH_RIGHT
                }
            }
        } else if (isIncoming(chatMessage)) {
            itemViewType = TYPE_TEXT_LEFT
        } else {
            itemViewType = TYPE_TEXT_RIGHT
        }

        return itemViewType
    }

    private fun isIncoming(chatMessage: QBChatMessage): Boolean {
        val currentUser = ChatHelper.getCurrentUser()
        return chatMessage.senderId != null && chatMessage.senderId != currentUser.id
    }

    /**
     * @return string in "Hours:Minutes" format, i.e. <b>10:15</b>
     */
    private fun getTime(seconds: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(seconds * 1000))
    }

    private fun getDate(seconds: Long): String {
        val dateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
        return dateFormat.format(Date(seconds * 1000))
    }

    private fun getImageUrl(position: Int): String {
        val attachment = getAttachment(position)
        return QBFile.getPrivateUrlForUID(attachment.id)
    }

    private fun getAttachment(position: Int): QBAttachment {
        val chatMessage = getItem(position)
        return chatMessage.attachments.iterator().next()
    }

    private fun showImageByURL(holder: MessageViewHolder, url: String) {
        val preferredImageWidth = context.resources.getDimension(R.dimen.attach_image_width_preview).toInt()
        val preferredImageHeight = context.resources.getDimension(R.dimen.attach_image_height_preview).toInt()

        Glide.with(context)
                .load(url)
                .listener(getRequestListener(holder))
                .override(preferredImageWidth, preferredImageHeight)
                .dontTransform()
                .error(R.drawable.ic_error)
                .into((holder as ImageAttachHolder).attachImageView)
    }

    private fun getRequestListener(holder: MessageViewHolder): RequestListener<String, GlideDrawable> {
        return ImageLoadListener(holder as ImageAttachHolder)
    }

    private inner class NotificationHolder : MessageViewHolder {
        var messageTimeTextView: TextView
        var messageTextView: TextView

        constructor(itemView: View,
                    @IdRes msgId: Int,
                    @IdRes timeId: Int) : super(itemView) {
            messageTimeTextView = itemView.findViewById(timeId)
            messageTextView = itemView.findViewById(msgId)
        }
    }

    private inner class TextMessageHolder : MessageViewHolder {
        var linkPreviewLayout: View
        var messageTimeTextView: TextView
        var messageTextView: TextView
        var messageStatusTextView: TextView? = null

        constructor(itemView: View,
                    @IdRes msgId: Int,
                    @IdRes timeId: Int,
                    @IdRes linkPreviewLayoutId: Int,
                    @IdRes statusId: Int) : super(itemView) {
            linkPreviewLayout = itemView.findViewById(linkPreviewLayoutId)
            messageTimeTextView = itemView.findViewById(timeId)
            messageTextView = itemView.findViewById(msgId)
            messageStatusTextView = itemView.findViewById(statusId)
        }

        constructor(itemView: View,
                    @IdRes msgId: Int,
                    @IdRes timeId: Int,
                    @IdRes linkPreviewLayoutId: Int) : super(itemView) {
            linkPreviewLayout = itemView.findViewById(linkPreviewLayoutId)
            messageTimeTextView = itemView.findViewById(timeId)
            messageTextView = itemView.findViewById(msgId)
        }
    }

    private inner class ImageAttachHolder : MessageViewHolder {
        var attachImageView: ImageView
        var attachmentProgressBar: ProgressBar
        var attachTextTime: TextView
        var attachStatusTextView: TextView? = null

        constructor(itemView: View,
                    @IdRes attachId: Int,
                    @IdRes progressBarId: Int,
                    @IdRes timeId: Int) : super(itemView) {
            attachImageView = itemView.findViewById(attachId)
            attachmentProgressBar = itemView.findViewById(progressBarId)
            attachTextTime = itemView.findViewById(timeId)
        }

        constructor(itemView: View,
                    @IdRes attachId: Int,
                    @IdRes progressBarId: Int,
                    @IdRes timeId: Int,
                    @IdRes statusId: Int) : super(itemView) {
            attachImageView = itemView.findViewById(attachId)
            attachmentProgressBar = itemView.findViewById(progressBarId)
            attachTextTime = itemView.findViewById(timeId)
            attachStatusTextView = itemView.findViewById(statusId)
        }
    }

    abstract inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var avatar: ImageView? = itemView.findViewById(R.id.msg_image_avatar)
        var bubbleFrame: View? = itemView.findViewById(R.id.msg_bubble_background)
    }

    private inner class ImageLoadListener<M, P>(val holder: ImageAttachHolder) : RequestListener<M, P> {

        init {
            holder.attachmentProgressBar.visibility = View.VISIBLE
        }

        override fun onException(e: Exception?, model: M, target: Target<P>, isFirstResource: Boolean): Boolean {
            Log.e(TAG, "ImageLoadListener Exception= $e")
            holder.attachImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            holder.attachmentProgressBar.visibility = View.GONE
            return false
        }

        override fun onResourceReady(resource: P, model: M, target: Target<P>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
            holder.attachImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.attachmentProgressBar.visibility = View.GONE
            return false
        }
    }

    private inner class ItemClickListenerFilter internal constructor(private val chatAttachClickListener:
                                                                     AttachClickListener,
                                                                     private val attachment: QBAttachment,
                                                                     private var position: Int) : View.OnClickListener {
        override fun onClick(view: View) {
            chatAttachClickListener.onLinkClicked(attachment, position)
        }
    }
}