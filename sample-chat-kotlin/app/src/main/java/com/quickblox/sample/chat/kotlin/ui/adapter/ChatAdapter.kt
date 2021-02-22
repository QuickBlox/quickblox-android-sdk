package com.quickblox.sample.chat.kotlin.ui.adapter

import android.content.Context
import android.graphics.Outline
import android.media.ThumbnailUtils
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quickblox.chat.model.QBAttachment
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.content.QBContent
import com.quickblox.content.model.QBFile
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.QBProgressCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.CollectionsUtil
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.managers.PROPERTY_NOTIFICATION_TYPE
import com.quickblox.sample.chat.kotlin.ui.activity.PROPERTY_FORWARD_USER_NAME
import com.quickblox.sample.chat.kotlin.ui.adapter.listeners.AttachClickListener
import com.quickblox.sample.chat.kotlin.ui.adapter.listeners.MessageLongClickListener
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.sample.chat.kotlin.utils.getColorCircleDrawable
import com.quickblox.sample.chat.kotlin.utils.getDateAsHeaderId
import com.quickblox.sample.chat.kotlin.utils.isAttachmentValid
import com.quickblox.sample.chat.kotlin.utils.qb.PaginationHistoryListener
import com.quickblox.sample.chat.kotlin.utils.qb.QbUsersHolder
import com.quickblox.sample.chat.kotlin.utils.shortToast
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import de.hdodenhof.circleimageview.CircleImageView
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

const val CUSTOM_VIEW_TYPE = -1
const val TYPE_TEXT_RIGHT = 1
const val TYPE_TEXT_LEFT = 2
const val TYPE_ATTACH_RIGHT = 3
const val TYPE_ATTACH_LEFT = 4
const val TYPE_NOTIFICATION_CENTER = 5

private const val FILE_DOWNLOAD_ATTEMPS_COUNT = 2
private const val ATTACHMENT_CORNER_RADIUS = 20f

class ChatAdapter(protected var context: Context,
                  private val chatDialog: QBChatDialog,
                  private val chatMessages: MutableList<QBChatMessage>) :
        RecyclerView.Adapter<ChatAdapter.NewMessageViewHolder>(),
        StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private val TAG = ChatAdapter::class.java.simpleName

    private var paginationListener: PaginationHistoryListener? = null
    private var previousGetCount = 0
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var attachImageClickListener: AttachClickListener? = null
    private var attachVideoClickListener: AttachClickListener? = null
    private var attachFileClickListener: AttachClickListener? = null
    private var messageLongClickListener: MessageLongClickListener? = null
    private var fileLoadingAttemptsMap = HashMap<String, Int>()

    private val containerLayoutRes = object : SparseIntArray() {
        init {
            put(TYPE_TEXT_RIGHT, R.layout.list_item_message_right)
            put(TYPE_TEXT_LEFT, R.layout.list_item_message_left)
            put(TYPE_ATTACH_RIGHT, R.layout.list_item_message_right)
            put(TYPE_ATTACH_LEFT, R.layout.list_item_message_left)
            put(TYPE_NOTIFICATION_CENTER, R.layout.list_item_notification_message)
            put(CUSTOM_VIEW_TYPE, R.layout.list_item_notification_message)
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

    fun setAttachVideoClickListener(clickListener: AttachClickListener) {
        attachVideoClickListener = clickListener
    }

    fun setAttachFileClickListener(clickListener: AttachClickListener) {
        attachFileClickListener = clickListener
    }

    fun setMessageLongClickListener(longClickListener: MessageLongClickListener) {
        messageLongClickListener = longClickListener
    }

    fun removeClickListeners() {
        attachImageClickListener = null
        attachVideoClickListener = null
        attachFileClickListener = null
        messageLongClickListener = null
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewMessageViewHolder {
        val currentView = inflater.inflate(containerLayoutRes.get(viewType), parent, false)
        return NewMessageViewHolder(currentView)
    }

    override fun onViewRecycled(holder: NewMessageViewHolder) {
        holder.ivVideoAttachPreview?.setImageBitmap(null)
        //abort loading avatar before setting new avatar to view
        if (containerLayoutRes.get(holder.itemViewType) != 0 && holder.avatar != null) {
            Glide.clear(holder.avatar)
        }
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: NewMessageViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder on position $position")
        downloadMore(position)
        val chatMessage = getItem(position)
        if (chatMessage != null && isIncoming(chatMessage) && !isReadByCurrentUser(chatMessage)) {
            readMessage(chatMessage)
        }

        if (getItemViewType(position) != TYPE_NOTIFICATION_CENTER) {
            messageLongClickListener?.let {
                holder.rootLayout?.setOnLongClickListener(ItemClickListenerFilter(getItemViewType(position), it, holder, position))
            }
        }

        when (getItemViewType(position)) {
            TYPE_NOTIFICATION_CENTER -> {
                onBindViewNotificationHolder(holder, chatMessage)
            }
            TYPE_TEXT_RIGHT -> {
                onBindViewMsgRightHolder(holder, chatMessage)
            }
            TYPE_TEXT_LEFT -> {
                onBindViewMsgLeftHolder(holder, chatMessage)
            }
            TYPE_ATTACH_RIGHT -> {
                onBindViewAttachRightHolder(holder, chatMessage, position)
            }
            TYPE_ATTACH_LEFT -> {
                onBindViewAttachLeftHolder(holder, chatMessage, position)
            }
            else -> {
                Log.d(TAG, "onBindViewHolder TYPE_ATTACHMENT_CUSTOM")
            }
        }
    }

    private fun onBindViewNotificationHolder(holder: NewMessageViewHolder, chatMessage: QBChatMessage?) {
        chatMessage?.let {
            holder.tvMessageBody?.text = chatMessage.body
            holder.tvMessageTime?.text = getTime(chatMessage.dateSent)
        }
    }

    private fun onBindViewMsgRightHolder(holder: NewMessageViewHolder, chatMessage: QBChatMessage?) {
        chatMessage?.let {
            holder.tvUserName?.text = context.getString(R.string.you)
            fillTextMessageHolder(holder, chatMessage, false)
        }
    }

    private fun onBindViewMsgLeftHolder(holder: NewMessageViewHolder, chatMessage: QBChatMessage?) {
        chatMessage?.let {
            holder.tvUserName?.text = getSenderName(chatMessage)
            fillTextMessageHolder(holder, chatMessage, true)
        }
    }

    private fun fillTextMessageHolder(holder: NewMessageViewHolder, chatMessage: QBChatMessage, isIncomingMessage: Boolean) {
        holder.rlImageAttachmentContainer?.visibility = View.GONE
        holder.rlVideoAttachmentContainer?.visibility = View.GONE
        holder.rlFileAttachmentContainer?.visibility = View.GONE
        holder.llMessageBodyContainer?.visibility = View.VISIBLE
        holder.tvMessageBody?.text = chatMessage.body
        holder.tvMessageTime?.text = getTime(chatMessage.dateSent)

        val forwardedFromName = chatMessage.getProperty(PROPERTY_FORWARD_USER_NAME) as String?
        if (forwardedFromName != null) {
            holder.llMessageBodyForwardContainer?.visibility = View.VISIBLE
            holder.tvTextForwardedFromUser?.text = forwardedFromName
        } else {
            holder.llMessageBodyForwardContainer?.visibility = View.GONE
        }

        if (chatDialog.type != QBDialogType.PRIVATE) {
            fillAvatarHolder(holder, chatMessage)
        } else {
            holder.avatarContainer?.visibility = View.GONE
        }

        if (!isIncomingMessage) {
            holder.avatarContainer?.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val read = isRead(chatMessage)
                val delivered = isDelivered(chatMessage)
                if (read) {
                    holder.ivMessageStatus?.setImageDrawable(context.getDrawable(R.drawable.ic_status_read))
                } else if (delivered) {

                    holder.ivMessageStatus?.setImageDrawable(context.getDrawable(R.drawable.ic_status_delivered))
                } else {
                    holder.ivMessageStatus?.setImageDrawable(context.getDrawable(R.drawable.ic_status_sent))
                }
            }
        }
    }

    private fun onBindViewAttachRightHolder(holder: NewMessageViewHolder, chatMessage: QBChatMessage?, position: Int) {
        chatMessage?.let {
            holder.tvMessageTime?.text = getTime(chatMessage.dateSent)
            holder.tvUserName?.text = context.getString(R.string.you)
            holder.avatarContainer?.visibility = View.GONE
            fillAttachHolder(holder, chatMessage, position, false)
        }
    }

    private fun onBindViewAttachLeftHolder(holder: NewMessageViewHolder, chatMessage: QBChatMessage?, position: Int) {
        chatMessage?.let {
            holder.tvMessageTime?.text = getTime(chatMessage.dateSent)
            holder.tvUserName?.text = getSenderName(chatMessage)

            if (chatDialog.type != QBDialogType.PRIVATE) {
                fillAvatarHolder(holder, chatMessage)
            } else {
                holder.avatarContainer?.visibility = View.GONE
            }
            fillAttachHolder(holder, chatMessage, position, true)
        }
    }

    private fun fillAvatarHolder(holder: NewMessageViewHolder, chatMessage: QBChatMessage) {
        holder.avatarContainer?.visibility = View.VISIBLE
        holder.avatarTitle?.visibility = View.VISIBLE
        holder.avatar?.setBackgroundDrawable(getColorCircleDrawable(chatMessage.senderId.hashCode()))

        val user = QbUsersHolder.getUserById(chatMessage.senderId)
        val avatarTitle = user?.fullName?.get(0).toString().toUpperCase()
        holder.avatarTitle?.text = avatarTitle
    }

    private fun fillAttachHolder(holder: NewMessageViewHolder, chatMessage: QBChatMessage, position: Int, isIncomingMessage: Boolean) {
        displayAttachment(holder, position, chatMessage)
        setItemAttachClickListener(getAttachListenerByType(position), holder, getAttachment(position), position)

        if (!isIncomingMessage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val read = isRead(chatMessage)
                val delivered = isDelivered(chatMessage)
                if (read) {
                    holder.ivMessageStatus?.setImageDrawable(context.getDrawable(R.drawable.ic_status_read))
                } else if (delivered) {

                    holder.ivMessageStatus?.setImageDrawable(context.getDrawable(R.drawable.ic_status_delivered))
                } else {
                    holder.ivMessageStatus?.setImageDrawable(context.getDrawable(R.drawable.ic_status_sent))
                }
            }
        }
    }

    private fun displayAttachment(holder: NewMessageViewHolder, position: Int, chatMessage: QBChatMessage) {
        val attachment = getAttachment(position)
        attachment?.let {
            val photo = QBAttachment.PHOTO_TYPE.equals(attachment.type, ignoreCase = true)
            val image = QBAttachment.IMAGE_TYPE.equals(attachment.type, ignoreCase = true)
            val video = QBAttachment.VIDEO_TYPE.equals(attachment.type, ignoreCase = true) || attachment.type.contains("video")
            val file = attachment.type == "file" || attachment.type.contains("file") || attachment.type == ""

            when {
                photo || image -> {
                    holder.llMessageBodyContainer?.visibility = View.GONE
                    holder.rlVideoAttachmentContainer?.visibility = View.GONE
                    holder.rlFileAttachmentContainer?.visibility = View.GONE
                    holder.rlImageAttachmentContainer?.visibility = View.VISIBLE

                    val forwardedFromName = chatMessage.getProperty(PROPERTY_FORWARD_USER_NAME) as String?
                    if (forwardedFromName != null) {
                        holder.llImageForwardContainer?.visibility = View.VISIBLE
                        holder.tvImageForwardedFromUser?.text = forwardedFromName
                    } else {
                        holder.llImageForwardContainer?.visibility = View.GONE
                    }

                    val imageUrl = QBFile.getPrivateUrlForUID(attachment.id)


                    Glide.with(context)
                            .load(imageUrl)
                            .listener(getRequestListener(holder))
                            .into(holder.ivImageAttachPreview)

                    makeRoundedCorners(holder.ivImageAttachPreview, false)
                }

                video -> {
                    holder.llMessageBodyContainer?.visibility = View.GONE
                    holder.rlVideoAttachmentContainer?.visibility = View.VISIBLE
                    holder.rlFileAttachmentContainer?.visibility = View.GONE
                    holder.rlImageAttachmentContainer?.visibility = View.GONE

                    holder.tvVideoFileName?.text = attachment.name
                    holder.tvVideoFileSize?.text = android.text.format.Formatter.formatShortFileSize(context, attachment.size.toLong())

                    val forwardedFromName = chatMessage.getProperty(PROPERTY_FORWARD_USER_NAME) as String?
                    if (forwardedFromName != null) {
                        holder.llVideoForwardContainer?.visibility = View.VISIBLE
                        holder.llVideoForwardedFromUser?.text = forwardedFromName
                    } else {
                        holder.llVideoForwardContainer?.visibility = View.GONE
                    }

                    fileLoadingAttemptsMap.put(attachment.id, 0)

                    if (attachment.name == null) {
                        return
                    }
                    val fileName = attachment.name
                    val file = File(context.filesDir, fileName)

                    if (file.exists()) {
                        fillVideoFileThumb(file, holder, position)
                    } else {
                        loadFileFromQB(holder, attachment, file, position)
                    }
                }

                file -> {
                    holder.llMessageBodyContainer?.visibility = View.GONE
                    holder.rlVideoAttachmentContainer?.visibility = View.GONE
                    holder.rlFileAttachmentContainer?.visibility = View.VISIBLE
                    holder.rlImageAttachmentContainer?.visibility = View.GONE

                    holder.tvFileName?.text = attachment.name
                    holder.tvFileSize?.text = android.text.format.Formatter.formatShortFileSize(context, attachment.size.toLong())

                    val forwardedFromName = chatMessage.getProperty(PROPERTY_FORWARD_USER_NAME) as String?
                    if (forwardedFromName != null) {
                        holder.llFileForwardContainer?.visibility = View.VISIBLE
                        holder.llFileForwardedFromUser?.text = forwardedFromName
                    } else {
                        holder.llFileForwardContainer?.visibility = View.GONE
                    }

                    fileLoadingAttemptsMap.put(attachment.id, 0)

                    val fileName = attachment.name
                    val file = File(context.filesDir, fileName)

                    if (!file.exists()) {
                        loadFileFromQB(holder, attachment, file, position)
                    }
                }

                else -> {
                    shortToast("Unknown Attachment Received")
                }
            }
        }
    }

    private fun loadFileFromQB(holder: NewMessageViewHolder, attachment: QBAttachment?, file: File, position: Int) {
        holder.videoProgress?.visibility = View.VISIBLE
        Log.d(TAG, "Loading File as Attachment id = " + attachment?.id)

        // to define download attempts count for each videofile
        if (attachment != null) {
            val attachmentID = attachment.id
            val attempts = fileLoadingAttemptsMap.get(attachmentID)
            fileLoadingAttemptsMap.set(attachmentID, (attempts!! + 1))

            QBContent.downloadFile(attachmentID, object : QBProgressCallback {
                override fun onProgressUpdate(progress: Int) {
                    holder.videoProgress?.progress = progress
                    Log.d(TAG, "Loading progress updated: $progress")
                }
            }, null).performAsync(object : QBEntityCallback<InputStream> {
                override fun onSuccess(inputStream: InputStream?, p1: Bundle?) {
                    Log.d(TAG, "Loading File as Attachment Successful")
                    if (inputStream != null) {
                        LoaderAsyncTask(file, inputStream, holder, position).execute()
                    }
                }

                override fun onError(e: QBResponseException?) {
                    Log.d(TAG, e?.message)
                    holder.videoProgress?.visibility = View.GONE
                }
            })
        }
    }

    private fun fillVideoFileThumb(file: File, holder: NewMessageViewHolder, position: Int) {
        val bitmap = ThumbnailUtils.createVideoThumbnail(file.path, MediaStore.Video.Thumbnails.MINI_KIND)

        val attachment = getAttachment(position)
        var attempts = fileLoadingAttemptsMap.get(attachment!!.id)
        if (attempts == null) {
            attempts = 0
        }

        if (bitmap == null && attempts <= FILE_DOWNLOAD_ATTEMPS_COUNT) {
            Log.d(TAG, "Thumbnail Bitmap is null from Downloaded File " + file.path)
            file.delete()
            Log.d(TAG, "Delete file and Reload")
            loadFileFromQB(holder, attachment, file, position)
        } else {
            holder.ivVideoAttachPreview?.setImageBitmap(bitmap)
            holder.videoProgress?.visibility = View.GONE

            makeRoundedCorners(holder.ivImageAttachPreview, true)
        }
    }

    private fun makeRoundedCorners(imageView: ImageView?, onlyTopCorners: Boolean) {
        val cornerRadius = ATTACHMENT_CORNER_RADIUS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && imageView != null) {
            imageView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    if (onlyTopCorners) {
                        outline?.setRoundRect(0, 0, view!!.width, (view.height + cornerRadius).toInt(), cornerRadius)
                    } else {
                        outline?.setRoundRect(0, 0, view!!.width, view.height, cornerRadius)
                    }
                }
            }
            imageView.clipToOutline = true
        }
    }

    private fun setItemAttachClickListener(listener: AttachClickListener?,
                                           holder: NewMessageViewHolder,
                                           qbAttachment: QBAttachment?,
                                           position: Int) {
        listener?.let {
            holder.rootLayout?.setOnClickListener(ItemClickListenerFilter(getItemViewType(position), it, holder, position))
        }
    }

    private fun getAttachListenerByType(position: Int): AttachClickListener? {
        val attachment = getAttachment(position)

        if (attachment != null) {
            if (QBAttachment.PHOTO_TYPE.equals(attachment.type, ignoreCase = true)
                    || QBAttachment.IMAGE_TYPE.equals(attachment.type, ignoreCase = true)) {
                return attachImageClickListener
            } else if (QBAttachment.VIDEO_TYPE.equals(attachment.type, ignoreCase = true)) {
                return attachVideoClickListener
            } else if (attachment.type == "file" || attachment.type == "" || attachment.type.contains("file")) {
                return attachFileClickListener
            } else {
                return null
            }
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
        val currentUserId = ChatHelper.getCurrentUser()!!.id
        return !CollectionsUtil.isEmpty(chatMessage.readIds) && chatMessage.readIds.contains(currentUserId)
    }

    private fun isRead(chatMessage: QBChatMessage): Boolean {
        var read = false
        val recipientId = chatMessage.recipientId
        val currentUserId = ChatHelper.getCurrentUser()!!.id
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
        val currentUserId = ChatHelper.getCurrentUser()!!.id
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
        return if (chatMessage != null) {
            getDateAsHeaderId(chatMessage.dateSent * 1000)
        } else {
            0L
        }
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
        chatMessage?.let {
            var title: String
            val timeInMillis = chatMessage.dateSent * 1000
            val msgTime = Calendar.getInstance()
            msgTime.timeInMillis = timeInMillis

            if (timeInMillis == 0L) {
                title = ""
            }

            val now = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("d MMM", Locale.ENGLISH)
            val lastYearFormat = SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH)

            val sameDay = now.get(Calendar.DATE) == msgTime.get(Calendar.DATE)
            val lastDay = now.get(Calendar.DAY_OF_YEAR) - msgTime.get(Calendar.DAY_OF_YEAR) == 1
            val sameYear = now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR)

            if (sameDay && sameYear) {
                title = context.getString(R.string.today)
            } else if (lastDay && sameYear) {
                title = context.getString(R.string.yesterday)
            } else if (sameYear) {
                title = dateFormat.format(Date(timeInMillis))
            } else {
                title = lastYearFormat.format(Date(timeInMillis))
            }

            dateTextView.text = title
        }

        val layoutParams = dateTextView.layoutParams as LinearLayout.LayoutParams
        layoutParams.topMargin = 0
        dateTextView.layoutParams = layoutParams
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    private fun getItem(position: Int): QBChatMessage? {
        if (position <= itemCount - 1) {
            return chatMessages[position]
        } else {
            return null
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        val chatMessage = getItem(position)
        var itemViewType = CUSTOM_VIEW_TYPE

        chatMessage?.let {
            if (chatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE) != null) {
                itemViewType = TYPE_NOTIFICATION_CENTER
            } else if (chatMessage.attachments?.isNotEmpty() == true) {
                val attachment = getAttachment(position)
                val photo = QBAttachment.PHOTO_TYPE.equals(attachment?.type, ignoreCase = true)
                val image = QBAttachment.IMAGE_TYPE.equals(attachment?.type, ignoreCase = true)
                val video = QBAttachment.VIDEO_TYPE.equals(attachment?.type, ignoreCase = true)
                val audio = QBAttachment.AUDIO_TYPE.equals(attachment?.type, ignoreCase = true)
                val file = attachment?.type == "file" || attachment?.type!!.contains("file") || attachment.type == ""

                if (photo || image || video || audio || file) {
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
        }

        return itemViewType
    }

    private fun isIncoming(chatMessage: QBChatMessage): Boolean {
        val currentUser = ChatHelper.getCurrentUser()
        return chatMessage.senderId != null && chatMessage.senderId != currentUser!!.id
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

    private fun getAttachment(position: Int): QBAttachment? {
        val chatMessage = getItem(position)
        if (chatMessage?.attachments?.iterator() != null && chatMessage.attachments.iterator().hasNext()) {
            return chatMessage.attachments.iterator().next()
        }
        return null
    }

    private fun getRequestListener(holder: NewMessageViewHolder): RequestListener<String, GlideDrawable> {
        return ImageLoadListener(holder)
    }

    inner class NewMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rootLayout: LinearLayout? = itemView.findViewById(R.id.ll_root_message_item)

        var avatarContainer: RelativeLayout? = itemView.findViewById(R.id.rl_avatar_container)
        var avatar: CircleImageView? = itemView.findViewById(R.id.civ_avatar)
        var avatarTitle: TextView? = itemView.findViewById(R.id.tv_avatar_title)

        var llMessageContainer: LinearLayout? = itemView.findViewById(R.id.ll_message_container)
        var llMessageBodyContainer: LinearLayout? = itemView.findViewById(R.id.ll_message_body_container)
        var llMessageBodyForwardContainer: LinearLayout? = itemView.findViewById(R.id.ll_forwarded_container)
        var tvTextForwardedFromUser: TextView? = itemView.findViewById(R.id.tv_forwarded_from_user)
        var tvMessageBody: TextView? = itemView.findViewById(R.id.tv_message_body)

        var rlImageAttachmentContainer: RelativeLayout? = itemView.findViewById(R.id.rl_image_attach_container)
        var ivImageAttachPreview: ImageView? = itemView.findViewById(R.id.iv_attach_image_preview)
        var llImageForwardContainer: LinearLayout? = itemView.findViewById(R.id.ll_image_forwarded_container)
        var tvImageForwardedFromUser: TextView? = itemView.findViewById(R.id.tv_image_forward_from_user)
        var pbImageProgress: ProgressBar? = itemView.findViewById(R.id.pb_attach_image)

        var rlVideoAttachmentContainer: RelativeLayout? = itemView.findViewById(R.id.rl_video_attach_container)
        var llVideoForwardContainer: LinearLayout? = itemView.findViewById(R.id.ll_video_forwarded_container)
        var llVideoForwardedFromUser: TextView? = itemView.findViewById(R.id.tv_video_forward_from_user)
        var ivVideoAttachPreview: ImageView? = itemView.findViewById(R.id.iv_attach_video_preview)
        var tvVideoFileName: TextView? = itemView.findViewById(R.id.tv_attach_video_name)
        var tvVideoFileSize: TextView? = itemView.findViewById(R.id.tv_attach_video_size)
        var videoProgress: ProgressBar? = itemView.findViewById(R.id.pb_attach_video)

        var rlFileAttachmentContainer: RelativeLayout? = itemView.findViewById(R.id.rl_file_attach_container)
        var llFileForwardContainer: LinearLayout? = itemView.findViewById(R.id.ll_file_forwarded_container)
        var llFileForwardedFromUser: TextView? = itemView.findViewById(R.id.tv_file_forward_from_user)
        var tvFileName: TextView? = itemView.findViewById(R.id.tv_attach_file_name)
        var tvFileSize: TextView? = itemView.findViewById(R.id.tv_attach_file_size)

        var tvUserName: TextView? = itemView.findViewById(R.id.tv_user_name)
        var tvMessageTime: TextView? = itemView.findViewById(R.id.tv_time_sent)

        val ivMessageStatus: ImageView? = itemView.findViewById(R.id.iv_message_status)
    }

    private inner class ImageLoadListener<M, P>(val holder: NewMessageViewHolder) : RequestListener<M, P> {

        init {
            holder.pbImageProgress?.visibility = View.VISIBLE
        }

        override fun onException(e: Exception?, model: M, target: Target<P>, isFirstResource: Boolean): Boolean {
            Log.e(TAG, "ImageLoadListener Exception= $e")
            holder.ivImageAttachPreview?.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.pbImageProgress?.visibility = View.GONE
            return false
        }

        override fun onResourceReady(resource: P, model: M, target: Target<P>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
            holder.ivImageAttachPreview?.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.pbImageProgress?.visibility = View.GONE
            return false
        }
    }

    private inner class ItemClickListenerFilter : View.OnClickListener, View.OnLongClickListener {

        private lateinit var attachClickListener: AttachClickListener
        private lateinit var messageLongClickListener: MessageLongClickListener
        private var holder: NewMessageViewHolder
        private var itemViewType: Int? = 0
        private var position: Int = 0

        constructor(itemViewType: Int?,
                    attachClickListener: AttachClickListener,
                    holder: NewMessageViewHolder,
                    position: Int) {
            this.itemViewType = itemViewType
            this.attachClickListener = attachClickListener
            this.holder = holder
            this.position = position
        }

        constructor(itemViewType: Int,
                    messageLongClickListener: MessageLongClickListener,
                    holder: NewMessageViewHolder,
                    position: Int) {
            this.itemViewType = itemViewType
            this.messageLongClickListener = messageLongClickListener
            this.holder = holder
            this.position = position
        }

        override fun onClick(view: View) {
            holder.llMessageContainer?.let {
                val iterator = getItem(position)?.attachments?.iterator()
                if (iterator != null && iterator.hasNext()) {
                    val attachment = iterator.next()
                    if (isAttachmentValid(attachment)) {
                        attachClickListener.onAttachmentClicked(itemViewType, it, attachment)
                    } else {
                        shortToast(context.getString(R.string.error_attachment_corrupted))
                    }
                }
            }
        }

        override fun onLongClick(view: View): Boolean {
            holder.llMessageContainer?.let {
                val message = getItem(position)
                messageLongClickListener.onMessageLongClicked(itemViewType, it, message)
            }
            return true
        }
    }

    private inner class LoaderAsyncTask internal constructor(private val file: File,
                                                             private val inputStream: InputStream,
                                                             private val holder: NewMessageViewHolder,
                                                             private val position: Int) : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            Log.d(TAG, "Downloading File as InputStream")
            val output = FileOutputStream(file)

            try {
                inputStream.use {
                    output.use {
                        inputStream?.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, e.message)
            }

            return true
        }

        override fun onPostExecute(result: Boolean?) {
            Log.d(TAG, "File Downloaded")
            fillVideoFileThumb(file, holder, position)
        }
    }
}