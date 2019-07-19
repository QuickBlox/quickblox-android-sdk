package com.quickblox.sample.chat.kotlin.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBMessageStatusesManager
import com.quickblox.chat.QBSystemMessagesManager
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBMessageStatusListener
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.QBAttachment
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.content.model.QBFile
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.GenericQueryRule
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.managers.DialogsManager
import com.quickblox.sample.chat.kotlin.ui.adapter.AttachmentPreviewAdapter
import com.quickblox.sample.chat.kotlin.ui.adapter.ChatAdapter
import com.quickblox.sample.chat.kotlin.ui.adapter.listeners.AttachClickListener
import com.quickblox.sample.chat.kotlin.ui.dialog.ProgressDialogFragment
import com.quickblox.sample.chat.kotlin.ui.views.AttachmentPreviewAdapterView
import com.quickblox.sample.chat.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.chat.kotlin.utils.SystemPermissionHelper
import com.quickblox.sample.chat.kotlin.utils.chat.CHAT_HISTORY_ITEMS_PER_PAGE
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.sample.chat.kotlin.utils.imagepick.OnImagePickedListener
import com.quickblox.sample.chat.kotlin.utils.imagepick.pickAnImage
import com.quickblox.sample.chat.kotlin.utils.qb.*
import com.quickblox.sample.chat.kotlin.utils.shortToast
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smackx.muc.DiscussionHistory
import java.io.File

private const val REQUEST_CODE_ATTACHMENT = 721
private const val REQUEST_CODE_SELECT_PEOPLE = 752

private const val PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST = 1010

const val EXTRA_DIALOG_ID = "dialogId"
const val EXTRA_IS_NEW_DIALOG = "isNewDialog"

private const val ORDER_RULE = "order"
private const val ORDER_VALUE = "desc string created_at"

class ChatActivity : BaseActivity(), OnImagePickedListener, QBMessageStatusListener, DialogsManager.ManagingDialogsCallbacks {
    private val TAG = ChatActivity::class.java.simpleName

    private lateinit var progressBar: ProgressBar
    private lateinit var messageEditText: EditText
    private lateinit var attachmentBtnChat: ImageButton

    private lateinit var attachmentPreviewContainerLayout: LinearLayout
    private lateinit var chatMessagesRecyclerView: RecyclerView

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var attachmentPreviewAdapter: AttachmentPreviewAdapter
    private lateinit var chatConnectionListener: ConnectionListener
    private lateinit var imageAttachClickListener: ImageAttachClickListener
    private lateinit var qbMessageStatusesManager: QBMessageStatusesManager
    private var chatMessageListener: ChatMessageListener = ChatMessageListener()
    private var dialogsManager: DialogsManager = DialogsManager()
    private var systemMessagesListener: SystemMessagesListener = SystemMessagesListener()
    private lateinit var systemMessagesManager: QBSystemMessagesManager

    private lateinit var messagesList: MutableList<QBChatMessage>
    private lateinit var qbChatDialog: QBChatDialog
    private var unShownMessages: ArrayList<QBChatMessage>? = null
    private var skipPagination = 0
    private var checkAdapterInit: Boolean = false

    companion object {
        fun startForResult(activity: Activity, code: Int, dialogId: QBChatDialog) {
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra(EXTRA_DIALOG_ID, dialogId)
            activity.startActivityForResult(intent, code)
        }

        fun startForResult(activity: Activity, code: Int, dialogId: QBChatDialog, isNewDialog: Boolean) {
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra(EXTRA_DIALOG_ID, dialogId)
            intent.putExtra(EXTRA_IS_NEW_DIALOG, isNewDialog)
            activity.startActivityForResult(intent, code)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        Log.v(TAG, "onCreate ChatActivity on Thread ID = " + Thread.currentThread().id)

        if (!ChatHelper.isLogged()) {
            Log.w(TAG, "Restarting App...")
            restartApp(this)
        }

        qbChatDialog = intent.getSerializableExtra(EXTRA_DIALOG_ID) as QBChatDialog

        Log.v(TAG, "Deserialized dialog = $qbChatDialog")

        try {
            qbChatDialog.initForChat(QBChatService.getInstance())
        } catch (e: IllegalStateException) {
            Log.v(TAG, "The error registerCallback fro chat. Error message is : " + e.message)
            finish()
        }
        qbChatDialog.addMessageListener(chatMessageListener)

        initViews()
        initMessagesRecyclerView()
        initChatConnectionListener()
        initChat()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putString(EXTRA_DIALOG_ID, qbChatDialog.dialogId)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val dialogId = savedInstanceState.getString(EXTRA_DIALOG_ID)!!
        qbChatDialog = QbDialogHolder.getChatDialogById(dialogId)!!
    }

    override fun onResumeFinished() {
        if (ChatHelper.isLogged()) {
            if (!::qbChatDialog.isInitialized) {
                qbChatDialog = intent.getSerializableExtra(EXTRA_DIALOG_ID) as QBChatDialog
            }
            qbChatDialog.initForChat(QBChatService.getInstance())
            qbChatDialog.join(DiscussionHistory())
            returnListeners()
        } else {
            showProgressDialog(R.string.dlg_loading)
            ChatHelper.loginToChat(SharedPrefsHelper.getQbUser()!!, object : QBEntityCallback<Void> {
                override fun onSuccess(aVoid: Void, bundle: Bundle) {
                    qbChatDialog.initForChat(QBChatService.getInstance())
                    returnListeners()
                    hideProgressDialog()
                }

                override fun onError(e: QBResponseException) {
                    hideProgressDialog()
                    finish()
                }
            })
        }
    }

    fun returnListeners() {
        dialogsManager.addManagingDialogsCallbackListener(this)
        systemMessagesManager = QBChatService.getInstance().systemMessagesManager
        systemMessagesManager.addSystemMessageListener(systemMessagesListener)
        chatAdapter.setAttachImageClickListener(imageAttachClickListener)
        ChatHelper.addConnectionListener(chatConnectionListener)
        qbMessageStatusesManager = QBChatService.getInstance().messageStatusesManager
        qbMessageStatusesManager.addMessageStatusListener(this)
    }

    override fun onPause() {
        super.onPause()
        chatAdapter.removeAttachImageClickListener()
        ChatHelper.removeConnectionListener(chatConnectionListener)
        qbMessageStatusesManager.removeMessageStatusListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        systemMessagesManager.removeSystemMessageListener(systemMessagesListener)
        qbChatDialog.removeMessageListrener(chatMessageListener)
        dialogsManager.removeManagingDialogsCallbackListener(this)
    }

    override fun onBackPressed() {
        qbChatDialog.removeMessageListrener(chatMessageListener)
        sendDialogId()
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_chat, menu)

        val menuItemLeave = menu.findItem(R.id.menu_chat_action_leave)
        val menuItemAdd = menu.findItem(R.id.menu_chat_action_add)
        val menuItemDelete = menu.findItem(R.id.menu_chat_action_delete)

        when (qbChatDialog.type) {
            QBDialogType.PRIVATE -> {
                menuItemLeave.isVisible = false
                menuItemAdd.isVisible = false
            }
            else -> menuItemDelete.isVisible = false
        }

        if (qbChatDialog.type != QBDialogType.GROUP) {
            menuItemAdd.isVisible = false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_chat_action_info -> {
                ChatInfoActivity.start(this, qbChatDialog)
                return true
            }
            R.id.menu_chat_action_add -> {
                updateDialog()
                return true
            }
            R.id.menu_chat_action_leave -> {
                leaveGroupChat()
                return true
            }
            R.id.menu_chat_action_delete -> {
                deleteChat()
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateDialog() {
        ProgressDialogFragment.show(supportFragmentManager)
        Log.d(TAG, "Starting Dialog Update")
        ChatHelper.getDialogById(qbChatDialog.dialogId, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(updatedChatDialog: QBChatDialog, bundle: Bundle) {
                Log.d(TAG, "Update Dialog Successful: " + updatedChatDialog.dialogId)
                qbChatDialog = updatedChatDialog
                loadUsersFromQb(updatedChatDialog)
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "Dialog Loading Error: " + e.message)
                ProgressDialogFragment.hide(supportFragmentManager)
                showErrorSnackbar(R.string.select_users_get_dialog_error, e, null)
            }
        })
    }

    private fun loadUsersFromQb(qbChatDialog: QBChatDialog) {
        val rules = ArrayList<GenericQueryRule>()
        rules.add(GenericQueryRule(ORDER_RULE, ORDER_VALUE))

        val qbPagedRequestBuilder = QBPagedRequestBuilder()
        qbPagedRequestBuilder.rules = rules
        qbPagedRequestBuilder.perPage = 100

        Log.d(TAG, "Loading Users")
        QBUsers.getUsers(qbPagedRequestBuilder).performAsync(object : QBEntityCallback<java.util.ArrayList<QBUser>> {
            override fun onSuccess(users: ArrayList<QBUser>, params: Bundle) {
                Log.d(TAG, "Loading Users Successful")
                ProgressDialogFragment.hide(supportFragmentManager)
                if (qbChatDialog.occupants.size >= users.size) {
                    shortToast(R.string.added_users)
                } else {
                    SelectUsersActivity.startForResult(this@ChatActivity, REQUEST_CODE_SELECT_PEOPLE, qbChatDialog)
                }
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "Loading Users Error: " + e.message)
                ProgressDialogFragment.hide(supportFragmentManager)
                showErrorSnackbar(R.string.select_users_get_users_error, e, null)
            }
        })
    }

    private fun sendDialogId() {
        val intent = Intent().putExtra(EXTRA_DIALOG_ID, qbChatDialog.dialogId)
        setResult(Activity.RESULT_OK, intent)
    }

    private fun leaveGroupChat() {
        showProgressDialog(R.string.dlg_loading)
        dialogsManager.sendMessageLeftUser(qbChatDialog)
        dialogsManager.sendSystemMessageLeftUser(systemMessagesManager, qbChatDialog)
        Log.d(TAG, "Leaving Dialog")
        ChatHelper.exitFromDialog(qbChatDialog, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(qbDialog: QBChatDialog, bundle: Bundle?) {
                Log.d(TAG, "Leaving Dialog Successful: " + qbDialog.dialogId)
                hideProgressDialog()
                QbDialogHolder.deleteDialog(qbDialog)
                finish()
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "Leaving Dialog Error: " + e.message)
                hideProgressDialog()
                showErrorSnackbar(R.string.error_leave_chat, e, View.OnClickListener { leaveGroupChat() })
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult with resultCode: $resultCode requestCode: $requestCode")
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_PEOPLE && data != null) {
                progressBar.visibility = View.VISIBLE
                val selectedUsers = data.getSerializableExtra(EXTRA_QB_USERS) as ArrayList<QBUser>
                val existingOccupants = qbChatDialog.occupants
                val newUserIds = ArrayList<Int>()

                for (user in selectedUsers) {
                    if (!existingOccupants.contains(user.id)) {
                        newUserIds.add(user.id)
                    }
                }

                ChatHelper.getDialogById(qbChatDialog.dialogId, object : QBEntityCallback<QBChatDialog> {
                    override fun onSuccess(qbChatDialog: QBChatDialog, p1: Bundle?) {
                        progressBar.visibility = View.GONE
                        dialogsManager.sendMessageAddedUsers(qbChatDialog, newUserIds)
                        dialogsManager.sendSystemMessageAddedUser(systemMessagesManager, qbChatDialog, newUserIds)
                        qbChatDialog.let {
                            this@ChatActivity.qbChatDialog = it
                        }
                        updateDialog(selectedUsers)
                    }

                    override fun onError(e: QBResponseException?) {
                        progressBar.visibility = View.GONE
                        showErrorSnackbar(R.string.update_dialog_error, e, null)
                    }
                })
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST && grantResults[0] != -1) {
            openImagePicker()
        }
    }

    override fun onImagePicked(requestCode: Int, file: File) {
        when (requestCode) {
            REQUEST_CODE_ATTACHMENT -> attachmentPreviewAdapter.add(file)
        }
    }

    override fun onImagePickError(requestCode: Int, e: Exception) {
        showErrorSnackbar(0, e, null)
    }

    override fun onImagePickClosed(ignored: Int) {

    }

    fun onSendChatClick(view: View) {
        val totalAttachmentsCount = attachmentPreviewAdapter.count
        val uploadedAttachments = attachmentPreviewAdapter.uploadedAttachments
        if (uploadedAttachments.isNotEmpty()) {
            if (uploadedAttachments.size == totalAttachmentsCount) {
                for (attachment in uploadedAttachments) {
                    sendChatMessage(null, attachment)
                }
            } else {
                shortToast(R.string.chat_wait_for_attachments_to_upload)
            }
        }

        val text = messageEditText.text.toString().trim { it <= ' ' }
        if (!TextUtils.isEmpty(text)) {
            sendChatMessage(text, null)
        }
    }

    fun showMessage(message: QBChatMessage) {
        if (isAdapterConnected()) {
            chatAdapter.addMessage(message)
            chatMessagesRecyclerView.scrollToPosition(messagesList.size - 1)
        } else {
            delayShowMessage(message)
        }
    }

    private fun isAdapterConnected(): Boolean {
        return checkAdapterInit
    }

    private fun delayShowMessage(message: QBChatMessage) {
        if (unShownMessages == null) {
            unShownMessages = ArrayList()
        }
        unShownMessages!!.add(message)
    }

    private fun initViews() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        messageEditText = findViewById(R.id.edit_chat_message)
        progressBar = findViewById(R.id.progress_chat)
        attachmentPreviewContainerLayout = findViewById(R.id.layout_attachment_preview_container)

        attachmentBtnChat = findViewById(R.id.button_chat_attachment)
        attachmentBtnChat.setOnClickListener { openImagePicker() }

        attachmentPreviewAdapter = AttachmentPreviewAdapter(this, object : AttachmentPreviewAdapter.AttachmentCountChangedListener {
            override fun onAttachmentCountChanged(count: Int) {
                val visiblePreview = when (count) {
                    0 -> View.GONE
                    else -> View.VISIBLE
                }
                attachmentPreviewContainerLayout.visibility = visiblePreview
            }
        }, object : AttachmentPreviewAdapter.AttachmentUploadErrorListener {
            override fun onAttachmentUploadError(e: QBResponseException) {
                showErrorSnackbar(0, e, View.OnClickListener { v ->
                    pickAnImage(this@ChatActivity, REQUEST_CODE_ATTACHMENT)
                })
            }
        })
        val previewAdapterView = findViewById<AttachmentPreviewAdapterView>(R.id.adapter_view_attachment_preview)
        previewAdapterView.setAdapter(attachmentPreviewAdapter)
    }

    private fun openImagePicker() {
        val permissionHelper = SystemPermissionHelper(this)
        if (permissionHelper.isSaveImagePermissionGranted()) {
            pickAnImage(this, REQUEST_CODE_ATTACHMENT)
        } else {
            permissionHelper.requestPermissionsForSaveFileImage()
        }
    }

    private fun initMessagesRecyclerView() {
        chatMessagesRecyclerView = findViewById(R.id.list_chat_messages)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        chatMessagesRecyclerView.layoutManager = layoutManager

        messagesList = ArrayList()
        chatAdapter = ChatAdapter(this, qbChatDialog, messagesList)
        chatAdapter.setPaginationHistoryListener(PaginationListener())
        chatMessagesRecyclerView.addItemDecoration(StickyRecyclerHeadersDecoration(chatAdapter))

        chatMessagesRecyclerView.adapter = chatAdapter
        imageAttachClickListener = ImageAttachClickListener()
    }

    private fun sendChatMessage(text: String?, attachment: QBAttachment?) {
        if (ChatHelper.isLogged()) {
            val chatMessage = QBChatMessage()
            attachment?.let {
                chatMessage.addAttachment(it)
            } ?: run {
                chatMessage.body = text
            }

            chatMessage.setSaveToHistory(true)
            chatMessage.dateSent = System.currentTimeMillis() / 1000
            chatMessage.isMarkable = true

            if (qbChatDialog.type != QBDialogType.PRIVATE && !qbChatDialog.isJoined) {
                qbChatDialog.join(DiscussionHistory())
                shortToast(R.string.chat_still_joining)
                return
            }
            try {
                Log.d(TAG, "Sending Message with ID: " + chatMessage.id)
                qbChatDialog.sendMessage(chatMessage)

                if (qbChatDialog.type == QBDialogType.PRIVATE) {
                    showMessage(chatMessage)
                }

                attachment?.let {
                    attachmentPreviewAdapter.remove(it)
                } ?: run {
                    messageEditText.setText("")
                }
            } catch (e: SmackException.NotConnectedException) {
                Log.w(TAG, e)
                shortToast(R.string.chat_error_send_message)
            }
        } else {
            showProgressDialog(R.string.dlg_login)
            Log.d(TAG, "Relogin to Chat")
            ChatHelper.loginToChat(ChatHelper.getCurrentUser(),
                    object : QBEntityCallback<Void> {
                        override fun onSuccess(p0: Void?, p1: Bundle?) {
                            Log.d(TAG, "Relogin Successful")
                            sendChatMessage(text, attachment)
                            hideProgressDialog()
                        }

                        override fun onError(e: QBResponseException) {
                            Log.d(TAG, "Relogin Error: " + e.message)
                            hideProgressDialog()
                            shortToast(R.string.chat_send_message_error)
                        }
                    })
        }
    }

    private fun initChat() {
        when (qbChatDialog.type) {
            QBDialogType.GROUP,
            QBDialogType.PUBLIC_GROUP -> joinGroupChat()
            QBDialogType.PRIVATE -> loadDialogUsers()
            else -> {
                shortToast(String.format("%s %s", getString(R.string.chat_unsupported_type), qbChatDialog.type.name))
                finish()
            }
        }
    }

    private fun joinGroupChat() {
        progressBar.visibility = View.VISIBLE
        ChatHelper.join(qbChatDialog, object : QBEntityCallback<Void> {
            override fun onSuccess(result: Void?, b: Bundle?) {
                Log.d(TAG, "Joined to Dialog Successful")
                notifyUsersAboutCreatingDialog()
                hideProgressDialog()
                loadDialogUsers()
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "Joining Dialog Error: " + e.message)
                progressBar.visibility = View.GONE
                showErrorSnackbar(R.string.connection_error, e, null)
            }
        })
    }

    private fun notifyUsersAboutCreatingDialog() {
        if (intent.getBooleanExtra(EXTRA_IS_NEW_DIALOG, false)) {
            dialogsManager.sendMessageCreatedDialog(qbChatDialog)
            intent.removeExtra(EXTRA_IS_NEW_DIALOG)
        }
    }

    private fun updateDialog(selectedUsers: ArrayList<QBUser>) {
        ChatHelper.updateDialogUsers(qbChatDialog, selectedUsers, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(dialog: QBChatDialog, args: Bundle?) {
                qbChatDialog = dialog
                loadDialogUsers()
            }

            override fun onError(e: QBResponseException) {
                showErrorSnackbar(R.string.chat_info_add_people_error, e, View.OnClickListener { updateDialog(selectedUsers) })
            }
        })
    }

    private fun loadDialogUsers() {
        ChatHelper.getUsersFromDialog(qbChatDialog, object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(users: ArrayList<QBUser>, bundle: Bundle?) {
                setChatNameToActionBar()
                loadChatHistory()
            }

            override fun onError(e: QBResponseException) {
                showErrorSnackbar(R.string.chat_load_users_error, e, View.OnClickListener { loadDialogUsers() })
            }
        })
    }

    private fun setChatNameToActionBar() {
        val chatName = getDialogName(qbChatDialog)
        supportActionBar?.title = chatName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    private fun loadChatHistory() {
        ChatHelper.loadChatHistory(qbChatDialog, skipPagination, object : QBEntityCallback<ArrayList<QBChatMessage>> {
            override fun onSuccess(messages: ArrayList<QBChatMessage>, args: Bundle?) {
                // The newest messages should be in the end of list,
                // so we need to reverse list to show messages in the right order
                messages.reverse()
                if (checkAdapterInit) {
                    chatAdapter.addMessages(messages)
                } else {
                    checkAdapterInit = true
                    chatAdapter.setMessages(messages)
                    addDelayedMessagesToAdapter()
                }
                progressBar.visibility = View.GONE
            }

            override fun onError(e: QBResponseException) {
                progressBar.visibility = View.GONE
                skipPagination -= CHAT_HISTORY_ITEMS_PER_PAGE
                showErrorSnackbar(R.string.connection_error, e, null)
            }
        })
        skipPagination += CHAT_HISTORY_ITEMS_PER_PAGE
    }

    private fun addDelayedMessagesToAdapter() {
        unShownMessages?.let {
            if (it.isNotEmpty()) {
                val chatList = chatAdapter.getMessages()
                for (message in it) {
                    if (!chatList.contains(message)) {
                        chatAdapter.addMessage(message)
                    }
                }
            }
        }
    }

    private fun deleteChat() {
        ChatHelper.deleteDialog(qbChatDialog, object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                setResult(Activity.RESULT_OK)
                finish()
            }

            override fun onError(e: QBResponseException) {
                showErrorSnackbar(R.string.dialogs_deletion_error, e, View.OnClickListener { deleteChat() })
            }
        })
    }

    private fun initChatConnectionListener() {
        val rootView: View = findViewById(R.id.list_chat_messages)
        chatConnectionListener = object : VerboseQbChatConnectionListener(rootView) {
            override fun reconnectionSuccessful() {
                super.reconnectionSuccessful()
                skipPagination = 0
                when (qbChatDialog.type) {
                    QBDialogType.GROUP -> {
                        checkAdapterInit = false
                        // Join active room if we're in Group Chat
                        runOnUiThread { joinGroupChat() }
                    }
                }
            }
        }
    }

    override fun processMessageDelivered(messageID: String, dialogID: String, userID: Int?) {
        if (qbChatDialog.dialogId == dialogID && userID != null) {
            chatAdapter.updateStatusDelivered(messageID, userID)
        }
    }

    override fun processMessageRead(messageID: String, dialogID: String, userID: Int?) {
        if (qbChatDialog.dialogId == dialogID && userID != null) {
            chatAdapter.updateStatusRead(messageID, userID)
        }
    }

    override fun onDialogCreated(chatDialog: QBChatDialog) {

    }

    override fun onDialogUpdated(chatDialog: String) {

    }

    override fun onNewDialogLoaded(chatDialog: QBChatDialog) {

    }

    private inner class ChatMessageListener : QbChatDialogMessageListenerImpl() {
        override fun processMessage(s: String, qbChatMessage: QBChatMessage, integer: Int?) {
            Log.d(TAG, "Processing Received Message: " + qbChatMessage.body)
            showMessage(qbChatMessage)
        }
    }

    private inner class SystemMessagesListener : QBSystemMessageListener {
        override fun processMessage(qbChatMessage: QBChatMessage) {
            Log.d(TAG, "System Message Received: " + qbChatMessage.id)
            dialogsManager.onSystemMessageReceived(qbChatMessage)
        }

        override fun processError(e: QBChatException?, qbChatMessage: QBChatMessage?) {
            Log.d(TAG, "System Messages Error: " + e?.message + "With MessageID: " + qbChatMessage?.id)
        }
    }

    private inner class ImageAttachClickListener : AttachClickListener {
        override fun onLinkClicked(attachment: QBAttachment, positionInAdapter: Int) {
            val url = QBFile.getPrivateUrlForUID(attachment.id)
            AttachmentImageActivity.start(this@ChatActivity, url)
        }
    }

    private inner class PaginationListener : PaginationHistoryListener {
        override fun downloadMore() {
            Log.w(TAG, "Download More")
            loadChatHistory()
        }
    }
}