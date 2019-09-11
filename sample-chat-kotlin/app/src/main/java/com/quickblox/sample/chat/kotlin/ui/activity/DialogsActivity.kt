package com.quickblox.sample.chat.kotlin.ui.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.view.ActionMode
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBIncomingMessagesManager
import com.quickblox.chat.QBSystemMessagesManager
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.messages.services.QBPushManager
import com.quickblox.messages.services.SubscribeService
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.async.BaseAsyncTask
import com.quickblox.sample.chat.kotlin.managers.DialogsManager
import com.quickblox.sample.chat.kotlin.ui.adapter.DialogsAdapter
import com.quickblox.sample.chat.kotlin.utils.ACTION_NEW_FCM_EVENT
import com.quickblox.sample.chat.kotlin.utils.EXTRA_FCM_MESSAGE
import com.quickblox.sample.chat.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.sample.chat.kotlin.utils.qb.QbChatDialogMessageListenerImpl
import com.quickblox.sample.chat.kotlin.utils.qb.QbDialogHolder
import com.quickblox.sample.chat.kotlin.utils.qb.callback.QBPushSubscribeListenerImpl
import com.quickblox.sample.chat.kotlin.utils.qb.callback.QbEntityCallbackImpl
import com.quickblox.sample.chat.kotlin.utils.shortToast
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import java.lang.ref.WeakReference


class DialogsActivity : BaseActivity(), DialogsManager.ManagingDialogsCallbacks {
    private val TAG = DialogsActivity::class.java.simpleName

    private lateinit var fab: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var requestBuilder: QBRequestGetBuilder
    private lateinit var setOnRefreshListener: SwipyRefreshLayout
    private lateinit var menu: Menu
    private var skipRecords = 0
    private var isProcessingResultInProgress: Boolean = false
    private lateinit var pushBroadcastReceiver: BroadcastReceiver

    private lateinit var dialogsAdapter: DialogsAdapter
    private var allDialogsMessagesListener: QBChatDialogMessageListener = AllDialogsMessageListener()
    private var systemMessagesListener: SystemMessagesListener = SystemMessagesListener()
    private lateinit var systemMessagesManager: QBSystemMessagesManager
    private lateinit var incomingMessagesManager: QBIncomingMessagesManager
    private var dialogsManager: DialogsManager = DialogsManager()
    private lateinit var currentUser: QBUser

    private var currentActionMode: ActionMode? = null

    companion object {
        private const val REQUEST_SELECT_PEOPLE = 174
        private const val REQUEST_DIALOG_ID_FOR_UPDATE = 165
        private const val PLAY_SERVICES_REQUEST_CODE = 9000

        fun start(context: Context) {
            val intent = Intent(context, DialogsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogs)

        if (!ChatHelper.isLogged()) {
            Log.w(TAG, "Restarting App...")
            restartApp(this)
        }

        dialogsManager.addManagingDialogsCallbackListener(this)
        systemMessagesManager = QBChatService.getInstance().systemMessagesManager
        incomingMessagesManager = QBChatService.getInstance().incomingMessagesManager
        currentUser = ChatHelper.getCurrentUser()

        initUi()

        supportActionBar?.title = getString(R.string.dialogs_logged_in_as, currentUser.fullName)

        if (QbDialogHolder.dialogsMap.isNotEmpty()) {
            loadDialogsFromQb(true, true)
        } else {
            loadDialogsFromQb(false, true)
        }
    }

    override fun onResume() {
        super.onResume()
        checkPlayServicesAvailable()
        registerQbChatListeners()
        pushBroadcastReceiver = PushBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                IntentFilter(ACTION_NEW_FCM_EVENT))
    }

    private fun checkPlayServicesAvailable() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_REQUEST_CODE).show()
            } else {
                Log.i(TAG, "This device is not supported.")
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterQbChatListeners()
    }

    private fun registerQbChatListeners() {
        systemMessagesManager.addSystemMessageListener(systemMessagesListener)

        incomingMessagesManager.addDialogMessageListener(allDialogsMessagesListener)
        dialogsManager.addManagingDialogsCallbackListener(this)
    }

    private fun unregisterQbChatListeners() {
        incomingMessagesManager.removeDialogMessageListrener(allDialogsMessagesListener)
        systemMessagesManager.removeSystemMessageListener(systemMessagesListener)
        dialogsManager.removeManagingDialogsCallbackListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_dialogs, menu)
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (isProcessingResultInProgress) {
            return super.onOptionsItemSelected(item)
        }
        when (item.itemId) {
            R.id.menu_dialogs_action_logout -> {
                isProcessingResultInProgress = true
                item.isEnabled = false
                invalidateOptionsMenu()
                userLogout()
                return true
            }
            R.id.menu_appinfo -> {
                AppInfoActivity.start(this)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult with ResultCode: $resultCode RequestCode: $requestCode")
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_SELECT_PEOPLE -> {
                    val selectedUsers = data?.getSerializableExtra(EXTRA_QB_USERS) as ArrayList<QBUser>
                    var chatName = data.getStringExtra(EXTRA_CHAT_NAME)
                    if (isPrivateDialogExist(selectedUsers)) {
                        selectedUsers.remove(ChatHelper.getCurrentUser())
                        val existingPrivateDialog = QbDialogHolder.getPrivateDialogWithUser(selectedUsers[0])
                        isProcessingResultInProgress = false
                        existingPrivateDialog?.let {
                            ChatActivity.startForResult(this, REQUEST_DIALOG_ID_FOR_UPDATE, it)
                        }
                    } else {
                        showProgressDialog(R.string.create_chat)
                        if (TextUtils.isEmpty(chatName)) {
                            chatName = ""
                        }
                        createDialog(selectedUsers, chatName)
                    }
                }
                REQUEST_DIALOG_ID_FOR_UPDATE -> {
                    if (data != null) {
                        val dialogId = data.getStringExtra(EXTRA_DIALOG_ID)
                        loadUpdatedDialog(dialogId)
                    } else {
                        isProcessingResultInProgress = false
                        updateDialogsList()
                    }
                }
            }
        } else {
            updateDialogsAdapter()
        }
    }

    private fun isPrivateDialogExist(allSelectedUsers: ArrayList<QBUser>): Boolean {
        val selectedUsers = ArrayList<QBUser>()
        selectedUsers.addAll(allSelectedUsers)
        selectedUsers.remove(ChatHelper.getCurrentUser())
        return selectedUsers.size == 1 && QbDialogHolder.hasPrivateDialogWithUser(selectedUsers[0])
    }

    private fun loadUpdatedDialog(dialogId: String) {
        ChatHelper.getDialogById(dialogId, object : QbEntityCallbackImpl<QBChatDialog>() {
            override fun onSuccess(result: QBChatDialog, bundle: Bundle?) {
                QbDialogHolder.addDialog(result)
                updateDialogsAdapter()
                isProcessingResultInProgress = false
            }

            override fun onError(e: QBResponseException) {
                isProcessingResultInProgress = false
            }
        })
    }

    override fun startSupportActionMode(callback: ActionMode.Callback): ActionMode? {
        currentActionMode = super.startSupportActionMode(callback)
        return currentActionMode
    }

    private fun userLogout() {
        showProgressDialog(R.string.dlg_loading)
        ChatHelper.destroy()
        logout()
        SharedPrefsHelper.removeQbUser()
        LoginActivity.start(this)
        QbDialogHolder.clear()
        hideProgressDialog()
        finish()
    }

    private fun logout() {
        if (QBPushManager.getInstance().isSubscribedToPushes) {
            QBPushManager.getInstance().addListener(object : QBPushSubscribeListenerImpl() {
                override fun onSubscriptionDeleted(deleted: Boolean) {
                    logoutREST()
                    QBPushManager.getInstance().removeListener(this)
                }
            })
            SubscribeService.unSubscribeFromPushes(this)
        } else {
            logoutREST()
        }
    }

    private fun logoutREST() {
        Log.d(TAG, "SignOut")
        QBUsers.signOut().performAsync(null)
    }

    private fun updateDialogsList() {
        skipRecords = 0
        requestBuilder.skip = skipRecords
        loadDialogsFromQb(true, true)
    }

    fun onStartNewChatClick(view: View) {
        SelectUsersActivity.startForResult(this, REQUEST_SELECT_PEOPLE, null)
    }

    private fun initUi() {
        val emptyHintLayout = findViewById<LinearLayout>(R.id.layout_chat_empty)
        val dialogsListView: ListView = findViewById(R.id.list_dialogs_chats)
        progressBar = findViewById(R.id.progress_dialogs)
        fab = findViewById(R.id.fab_dialogs_new_chat)
        setOnRefreshListener = findViewById(R.id.swipy_refresh_layout)

        val dialogs = ArrayList(QbDialogHolder.dialogsMap.values)
        dialogsAdapter = DialogsAdapter(this, dialogs)

        val listHeader = LayoutInflater.from(this)
                .inflate(R.layout.include_list_hint_header, dialogsListView, false) as TextView
        listHeader.setText(R.string.dialogs_list_hint)

        dialogsListView.emptyView = emptyHintLayout
        dialogsListView.addHeaderView(listHeader, null, false)
        dialogsListView.adapter = dialogsAdapter

        dialogsListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedDialog = parent.getItemAtPosition(position) as QBChatDialog

            if (currentActionMode != null) {
                dialogsAdapter.toggleSelection(selectedDialog)
            } else if (ChatHelper.isLogged()) {
                ChatActivity.startForResult(this, REQUEST_DIALOG_ID_FOR_UPDATE, selectedDialog)
            } else {
                showProgressDialog(R.string.dlg_login)
                ChatHelper.loginToChat(ChatHelper.getCurrentUser(),
                        object : QBEntityCallback<Void> {
                            override fun onSuccess(p0: Void?, p1: Bundle?) {
                                hideProgressDialog()
                                ChatActivity.startForResult(this@DialogsActivity, REQUEST_DIALOG_ID_FOR_UPDATE, selectedDialog)
                            }

                            override fun onError(e: QBResponseException?) {
                                hideProgressDialog()
                                showErrorSnackbar(R.string.login_chat_login_error, e, null)
                            }
                        })
            }
        }

        dialogsListView.setOnItemLongClickListener { parent, view, position, id ->
            val selectedDialog = parent.getItemAtPosition(position) as QBChatDialog
            startSupportActionMode(DeleteActionModeCallback())
            dialogsAdapter.selectItem(selectedDialog)
            return@setOnItemLongClickListener true
        }

        requestBuilder = QBRequestGetBuilder()

        setOnRefreshListener.setOnRefreshListener {
            //Pagination
            //skipRecords += DIALOG_ITEMS_PER_PAGE
            //requestBuilder.skip = (skipRecords)

            loadDialogsFromQb(silentUpdate = true, clearDialogHolder = true)
        }
    }

    private fun createDialog(selectedUsers: ArrayList<QBUser>, chatName: String) {
        ChatHelper.createDialogWithSelectedUsers(selectedUsers, chatName,
                object : QBEntityCallback<QBChatDialog> {
                    override fun onSuccess(dialog: QBChatDialog, args: Bundle?) {
                        Log.d(TAG, "Creating Dialog Successful")
                        isProcessingResultInProgress = false
                        dialogsManager.sendSystemMessageAboutCreatingDialog(systemMessagesManager, dialog)
                        var dialogs = ArrayList<QBChatDialog>()
                        dialogs.add(dialog)
                        DialogJoinerAsyncTask(this@DialogsActivity, dialogs, false).execute()

                        ChatActivity.startForResult(this@DialogsActivity, REQUEST_DIALOG_ID_FOR_UPDATE, dialog, true)
                        hideProgressDialog()
                    }

                    override fun onError(error: QBResponseException) {
                        Log.d(TAG, "Creating Dialog Error: " + error.message)
                        isProcessingResultInProgress = false
                        hideProgressDialog()
                        showErrorSnackbar(R.string.dialogs_creation_error, error, null)
                    }
                }
        )
    }

    private fun loadDialogsFromQb(silentUpdate: Boolean, clearDialogHolder: Boolean) {
        isProcessingResultInProgress = true
        if (!silentUpdate) {
            progressBar.visibility = View.VISIBLE
        }
        ChatHelper.getDialogs(requestBuilder, object : QBEntityCallback<ArrayList<QBChatDialog>> {
            override fun onSuccess(dialogs: ArrayList<QBChatDialog>, bundle: Bundle?) {
                DialogJoinerAsyncTask(this@DialogsActivity, dialogs, clearDialogHolder).execute()
            }

            override fun onError(e: QBResponseException) {
                disableProgress()
                shortToast(e.message)
            }
        })
    }

    private fun disableProgress() {
        isProcessingResultInProgress = false
        progressBar.visibility = View.GONE
        setOnRefreshListener.isRefreshing = false
    }

    private fun updateDialogsAdapter() {
        val listDialogs = ArrayList(QbDialogHolder.dialogsMap.values)
        dialogsAdapter.updateList(listDialogs)
    }

    override fun onDialogCreated(chatDialog: QBChatDialog) {
        loadDialogsFromQb(true, true)
    }

    override fun onDialogUpdated(chatDialog: String) {
        updateDialogsAdapter()
    }

    override fun onNewDialogLoaded(chatDialog: QBChatDialog) {
        updateDialogsAdapter()
    }

    private inner class DeleteActionModeCallback internal constructor() : ActionMode.Callback {

        init {
            fab.hide()
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_mode_dialogs, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_dialogs_action_delete -> {
                    deleteSelectedDialogs()
                    currentActionMode?.finish()
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            currentActionMode = null
            dialogsAdapter.clearSelection()
            fab.show()
        }

        private fun deleteSelectedDialogs() {
            val selectedDialogs = dialogsAdapter.selectedItems
            for (dialog in selectedDialogs) {
                dialogsManager.sendMessageLeftUser(dialog)
                dialogsManager.sendSystemMessageLeftUser(systemMessagesManager, dialog)
            }

            ChatHelper.deleteDialogs(selectedDialogs, object : QBEntityCallback<ArrayList<String>> {
                override fun onSuccess(dialogsIds: ArrayList<String>, bundle: Bundle?) {
                    Log.d(TAG, "Dialogs Deleting Successful")
                    QbDialogHolder.deleteDialogs(dialogsIds)
                    updateDialogsAdapter()
                }

                override fun onError(e: QBResponseException) {
                    Log.d(TAG, "Deleting Dialogs Error: " + e.message)
                    showErrorSnackbar(R.string.dialogs_deletion_error, e,
                            View.OnClickListener { deleteSelectedDialogs() })
                }
            })
        }
    }

    private inner class PushBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val message = intent.getStringExtra(EXTRA_FCM_MESSAGE)
            Log.v(TAG, "Received broadcast " + intent.action + " with data: " + message)
            skipRecords = 0
            requestBuilder.skip = skipRecords
            loadDialogsFromQb(true, true)
        }
    }

    private inner class SystemMessagesListener : QBSystemMessageListener {
        override fun processMessage(qbChatMessage: QBChatMessage) {
            dialogsManager.onSystemMessageReceived(qbChatMessage)
        }

        override fun processError(e: QBChatException, qbChatMessage: QBChatMessage) {

        }
    }

    private inner class AllDialogsMessageListener : QbChatDialogMessageListenerImpl() {
        override fun processMessage(dialogID: String, qbChatMessage: QBChatMessage, senderID: Int?) {
            Log.d(TAG, "Processing received Message: " + qbChatMessage.body)
            if (senderID != ChatHelper.getCurrentUser().id) {
                dialogsManager.onGlobalMessageReceived(dialogID, qbChatMessage)
            }
        }
    }

    private class DialogJoinerAsyncTask internal constructor(dialogsActivity: DialogsActivity,
                                                             private val dialogs: ArrayList<QBChatDialog>,
                                                             private val clearDialogHolder: Boolean) : BaseAsyncTask<Void, Void, Void>() {
        private val activityRef: WeakReference<DialogsActivity> = WeakReference(dialogsActivity)

        @Throws(Exception::class)
        override fun performInBackground(vararg params: Void): Void? {
            ChatHelper.join(dialogs)
            return null
        }

        override fun onResult(result: Void?) {
            activityRef.get()?.disableProgress()
            if (clearDialogHolder) {
                QbDialogHolder.clear()
            }
            QbDialogHolder.addDialogs(dialogs)
            activityRef.get()?.updateDialogsAdapter()
        }

        override fun onException(e: Exception) {
            super.onException(e)
            Log.d("Dialog Joiner Task", "Error: $e")
            shortToast("Error: " + e.message)
        }
    }
}