package com.quickblox.sample.chat.kotlin.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.async.BaseAsyncTask
import com.quickblox.sample.chat.kotlin.managers.DialogsManager
import com.quickblox.sample.chat.kotlin.ui.adapter.DialogsAdapter
import com.quickblox.sample.chat.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.sample.chat.kotlin.utils.qb.QbDialogHolder
import com.quickblox.sample.chat.kotlin.utils.qb.QbUsersHolder
import com.quickblox.sample.chat.kotlin.utils.shortToast
import com.quickblox.users.model.QBUser
import org.jivesoftware.smack.SmackException
import java.lang.ref.WeakReference

const val EXTRA_FORWARD_MESSAGE = "extra_forward_message"

class ForwardToActivity : BaseActivity(), DialogsManager.ManagingDialogsCallbacks {
    private val TAG = ForwardToActivity::class.java.simpleName

    private lateinit var requestBuilder: QBRequestGetBuilder
    private lateinit var refreshLayout: SwipyRefreshLayout
    private lateinit var originMessage: QBChatMessage
    private lateinit var dialogsAdapter: DialogsAdapter

    private lateinit var currentUser: QBUser
    private lateinit var menu: Menu

    private var isProcessingResultInProgress: Boolean = false
    private var dialogsManager: DialogsManager = DialogsManager()
    private var hasMoreDialogs = true
    private var loadedDialogs = HashSet<QBChatDialog>()

    companion object {

        fun start(context: Context, messageToForward: QBChatMessage) {
            val intent = Intent(context, ForwardToActivity::class.java)
            intent.putExtra(EXTRA_FORWARD_MESSAGE, messageToForward)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogs)

        var progressBar = findViewById<ProgressBar>(R.id.pb_dialogs)
        progressBar.visibility = View.GONE

        if (!ChatHelper.isLogged()) {
            reloginToChat()
        }

        supportActionBar?.title = getString(R.string.forward_to)
        supportActionBar?.subtitle = getString(R.string.dialogs_actionmode_subtitle, "0")
        if (ChatHelper.getCurrentUser() != null) {
            currentUser = ChatHelper.getCurrentUser()!!
        } else {
            Log.e(TAG, "Finishing " + TAG + ". Not Logged in Chat.")
            finish()
        }

        originMessage = intent.getSerializableExtra(EXTRA_FORWARD_MESSAGE) as QBChatMessage
        requestBuilder = QBRequestGetBuilder()
        requestBuilder.limit = DIALOGS_PER_PAGE
        requestBuilder.skip = 0
        initUi()
    }

    override fun onResumeFinished() {
        if (ChatHelper.isLogged()) {
            loadDialogsFromQb()
        } else {
            reloginToChat()
        }
    }

    private fun reloginToChat() {
        showProgressDialog(R.string.dlg_loading)
        ChatHelper.loginToChat(SharedPrefsHelper.getQbUser()!!, object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void, bundle: Bundle) {
                Log.d(TAG, "Relogin Successful")
                loadDialogsFromQb()
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "Relogin Failed " + e.message)
                hideProgressDialog()
                showErrorSnackbar(R.string.reconnect_failed, e, View.OnClickListener { reloginToChat() })
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogsManager.removeManagingDialogsCallbackListener(this)
    }

    private fun initUi() {
        val emptyHintLayout = findViewById<LinearLayout>(R.id.ll_chat_empty)
        val dialogsListView: ListView = findViewById(R.id.list_dialogs_chats)
        refreshLayout = findViewById(R.id.swipy_refresh_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val dialogs = ArrayList(QbDialogHolder.dialogsMap.values)
        dialogsAdapter = DialogsAdapter(this, dialogs)
        dialogsAdapter.prepareToSelect()

        dialogsListView.emptyView = emptyHintLayout
        dialogsListView.adapter = dialogsAdapter

        dialogsListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedDialog = parent.getItemAtPosition(position) as QBChatDialog
            dialogsAdapter.toggleSelection(selectedDialog)
            menu.getItem(0).isVisible = (dialogsAdapter.selectedItems.size >= 1)
            supportActionBar?.subtitle = getString(R.string.dialogs_actionmode_subtitle, dialogsAdapter.selectedItems.size.toString())
        }

        refreshLayout.setOnRefreshListener {
            loadDialogsFromQb()
        }
        refreshLayout.setColorSchemeResources(R.color.color_new_blue, R.color.random_color_2, R.color.random_color_3, R.color.random_color_7)
        dialogsAdapter.clearSelection()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_activity_forward, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (isProcessingResultInProgress) {
            return super.onOptionsItemSelected(item)
        }
        when (item.itemId) {
            R.id.menu_send -> {
                showProgressDialog(R.string.dlg_sending)
                ForwardedMessageSenderAsyncTask(this, dialogsAdapter.selectedItems).execute()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun sendForwardedMessage(dialogs: ArrayList<QBChatDialog>) {
        for (dialog in dialogs) {
            try {
                val messageToForward = QBChatMessage()
                messageToForward.setSaveToHistory(true)
                messageToForward.dateSent = System.currentTimeMillis() / 1000
                messageToForward.isMarkable = true

                messageToForward.attachments = originMessage.attachments
                if (originMessage.body == null) {
                    messageToForward.body = null
                } else {
                    messageToForward.body = originMessage.body
                }

                var senderName = ""
                if (originMessage.senderId == currentUser.id) {
                    senderName = currentUser.fullName
                } else {
                    val sender = QbUsersHolder.getUserById(originMessage.senderId)
                    sender?.let {
                        senderName = it.fullName
                    }
                }
                messageToForward.setProperty(PROPERTY_FORWARD_USER_NAME, senderName)
                dialog.sendMessage(messageToForward)
            } catch (e: SmackException.NotConnectedException) {
                Log.d(TAG, "Send Forwarded Message Exception: " + e.message)
                shortToast(R.string.error_forwarding_not_connected)
            }
        }
        disableProgress()
        shortToast("Forwarding Complete")
        finish()
    }

    private fun loadDialogsFromQb() {
        isProcessingResultInProgress = true
        showProgressDialog(R.string.dlg_loading)

        ChatHelper.getDialogs(requestBuilder, object : QBEntityCallback<ArrayList<QBChatDialog>> {
            override fun onSuccess(dialogs: ArrayList<QBChatDialog>, bundle: Bundle?) {
                if (dialogs.size < DIALOGS_PER_PAGE) {
                    hasMoreDialogs = false
                }
                loadedDialogs.addAll(dialogs)
                QbDialogHolder.addDialogs(dialogs)
                updateDialogsAdapter()
                requestBuilder.skip = loadedDialogs.size
                if (hasMoreDialogs) {
                    loadDialogsFromQb()
                }
                disableProgress()
            }

            override fun onError(e: QBResponseException) {
                disableProgress()
                dialogsAdapter.clearSelection()
                shortToast(e.message)
            }
        })
    }

    private fun disableProgress() {
        isProcessingResultInProgress = false
        hideProgressDialog()
        refreshLayout.isRefreshing = false
    }

    private fun updateDialogsAdapter() {
        val listDialogs = ArrayList(QbDialogHolder.dialogsMap.values)
        dialogsAdapter.updateList(listDialogs)
        dialogsAdapter.prepareToSelect()
    }

    override fun onDialogCreated(chatDialog: QBChatDialog) {
        loadDialogsFromQb()
    }

    override fun onDialogUpdated(chatDialog: String) {
        updateDialogsAdapter()
    }

    override fun onNewDialogLoaded(chatDialog: QBChatDialog) {
        updateDialogsAdapter()
    }

    private class ForwardedMessageSenderAsyncTask internal constructor(forwardToActivity: ForwardToActivity,
                                                                       private val dialogs: ArrayList<QBChatDialog>) : BaseAsyncTask<Void, Void, Void>() {
        private val activityRef: WeakReference<ForwardToActivity> = WeakReference(forwardToActivity)

        @Throws(Exception::class)
        override fun performInBackground(vararg params: Void): Void? {
            ChatHelper.join(dialogs)
            return null
        }

        override fun onResult(result: Void?) {
            activityRef.get()?.sendForwardedMessage(dialogs)
        }

        override fun onException(e: Exception) {
            super.onException(e)
            Log.d("Dialog Joiner Task", "Error: $e")
            shortToast("Error: " + e.message)
        }
    }
}