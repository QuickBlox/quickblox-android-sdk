package com.quickblox.sample.chat.kotlin.ui.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Vibrator
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.view.ActionMode
import androidx.core.view.get
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBIncomingMessagesManager
import com.quickblox.chat.QBSystemMessagesManager
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.messages.services.QBPushManager
import com.quickblox.messages.services.SubscribeService
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.async.BaseAsyncTask
import com.quickblox.sample.chat.kotlin.managers.DialogsManager
import com.quickblox.sample.chat.kotlin.ui.adapter.DialogsAdapter
import com.quickblox.sample.chat.kotlin.utils.*
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.sample.chat.kotlin.utils.qb.QbChatDialogMessageListenerImpl
import com.quickblox.sample.chat.kotlin.utils.qb.QbDialogHolder
import com.quickblox.sample.chat.kotlin.utils.qb.VerboseQbChatConnectionListener
import com.quickblox.sample.chat.kotlin.utils.qb.callback.QBPushSubscribeListenerImpl
import com.quickblox.sample.chat.kotlin.utils.qb.callback.QbEntityCallbackImpl
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import org.jivesoftware.smack.ConnectionListener
import java.lang.ref.WeakReference

const val DIALOGS_PER_PAGE = 100

class DialogsActivity : BaseActivity(), DialogsManager.ManagingDialogsCallbacks {
    private val TAG = DialogsActivity::class.java.simpleName

    private lateinit var refreshLayout: SwipyRefreshLayout
    private lateinit var progress: ProgressBar
    private var isProcessingResultInProgress: Boolean = false
    private lateinit var pushBroadcastReceiver: BroadcastReceiver
    private lateinit var chatConnectionListener: ConnectionListener

    private lateinit var dialogsAdapter: DialogsAdapter
    private var allDialogsMessagesListener: QBChatDialogMessageListener = AllDialogsMessageListener()
    private var systemMessagesListener: SystemMessagesListener = SystemMessagesListener()
    private lateinit var systemMessagesManager: QBSystemMessagesManager
    private lateinit var incomingMessagesManager: QBIncomingMessagesManager
    private var dialogsManager: DialogsManager = DialogsManager()
    private lateinit var currentUser: QBUser

    private var currentActionMode: ActionMode? = null
    private var hasMoreDialogs = true
    private val joinerTasksSet = HashSet<DialogJoinerAsyncTask>()

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
            reloginToChat()
        }

        if (ChatHelper.getCurrentUser() != null) {
            currentUser = ChatHelper.getCurrentUser()!!
        } else {
            Log.e(TAG, "Finishing " + TAG + ". Current user is null")
            finish()
        }
        supportActionBar?.title = getString(R.string.dialogs_logged_in_as, currentUser.fullName)
        initUi()
        initConnectionListener()
    }

    override fun onResumeFinished() {
        if (ChatHelper.isLogged()) {
            checkPlayServicesAvailable()
            registerQbChatListeners()
            if (QbDialogHolder.dialogsMap.isNotEmpty()) {
                loadDialogsFromQb(true, true)
            } else {
                loadDialogsFromQb(false, true)
            }
        } else {
            reloginToChat()
        }
    }

    private fun reloginToChat() {
        showProgressDialog(R.string.dlg_relogin)
        if (SharedPrefsHelper.hasQbUser()) {
            ChatHelper.loginToChat(SharedPrefsHelper.getQbUser()!!, object : QBEntityCallback<Void> {
                override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                    Log.d(TAG, "Relogin Successful")
                    checkPlayServicesAvailable()
                    registerQbChatListeners()
                    loadDialogsFromQb(false, false)
                }

                override fun onError(e: QBResponseException) {
                    Log.d(TAG, "Relogin Failed " + e.message)
                    hideProgressDialog()
                    showErrorSnackbar(R.string.reconnect_failed, e, View.OnClickListener { reloginToChat() })
                }
            })
        }
    }

    private fun checkPlayServicesAvailable() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_REQUEST_CODE).show()
            } else {
                Log.e(TAG, "This device is not supported.")
                shortToast("This device is not supported")
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        ChatHelper.removeConnectionListener(chatConnectionListener)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver)
    }

    override fun onStop() {
        super.onStop()
        cancelTasks()
        unregisterQbChatListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterQbChatListeners()
    }

    private fun registerQbChatListeners() {
        ChatHelper.addConnectionListener(chatConnectionListener)
        try {
            systemMessagesManager = QBChatService.getInstance().systemMessagesManager
            incomingMessagesManager = QBChatService.getInstance().incomingMessagesManager
        } catch (e: Exception) {
            Log.d(TAG, "Can not get SystemMessagesManager. Need relogin. " + e.message)
            reloginToChat()
            return
        }

        if (incomingMessagesManager == null) {
            reloginToChat()
            return
        }
        systemMessagesManager.addSystemMessageListener(systemMessagesListener)
        incomingMessagesManager.addDialogMessageListener(allDialogsMessagesListener)
        dialogsManager.addManagingDialogsCallbackListener(this)

        pushBroadcastReceiver = PushBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                IntentFilter(ACTION_NEW_FCM_EVENT))
    }

    private fun unregisterQbChatListeners() {
        incomingMessagesManager.removeDialogMessageListrener(allDialogsMessagesListener)
        systemMessagesManager.removeSystemMessageListener(systemMessagesListener)
        dialogsManager.removeManagingDialogsCallbackListener(this)
    }

    private fun cancelTasks() {
        joinerTasksSet.iterator().forEach {
            it.cancel(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_dialogs, menu)
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
                unsubscribeFromPushes()
                return true
            }
            R.id.menu_appinfo -> {
                AppInfoActivity.start(this)
                return true
            }
            R.id.menu_add_chat -> {
                showProgressDialog(R.string.dlg_loading)
                SelectUsersActivity.startForResult(this, REQUEST_SELECT_PEOPLE, null)
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
                        loadDialogsFromQb(true, false)
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
        Log.d(TAG, "SignOut")
        QBUsers.signOut().performAsync(object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                ChatHelper.destroy()
                SharedPrefsHelper.removeQbUser()
                QbDialogHolder.clear()
                LoginActivity.start(this@DialogsActivity)
                hideProgressDialog()
                finish()
            }

            override fun onError(e: QBResponseException?) {
                Log.d(TAG, "Unable to SignOut: " + e?.message)
                hideProgressDialog()
                showErrorSnackbar(R.string.error_logout, e, View.OnClickListener { userLogout() })
            }
        })
    }

    private fun unsubscribeFromPushes() {
        showProgressDialog(R.string.dlg_logout)
        if (QBPushManager.getInstance().isSubscribedToPushes) {
            QBPushManager.getInstance().addListener(object : QBPushSubscribeListenerImpl() {
                override fun onSubscriptionDeleted(success: Boolean) {
                    Log.d(TAG, "Subscription Deleted")
                    QBPushManager.getInstance().removeListener(this)
                    userLogout()
                }
            })
            SubscribeService.unSubscribeFromPushes(this@DialogsActivity)
        } else {
            userLogout()
        }
    }

    private fun initUi() {
        val emptyHintLayout = findViewById<LinearLayout>(R.id.ll_chat_empty)
        val dialogsListView: ListView = findViewById(R.id.list_dialogs_chats)
        refreshLayout = findViewById(R.id.swipy_refresh_layout)
        progress = findViewById(R.id.pb_dialogs)

        val dialogs = ArrayList(QbDialogHolder.dialogsMap.values)
        dialogsAdapter = DialogsAdapter(this, dialogs)

        dialogsListView.emptyView = emptyHintLayout
        dialogsListView.adapter = dialogsAdapter

        dialogsListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedDialog = parent.getItemAtPosition(position) as QBChatDialog
            if (currentActionMode != null) {
                dialogsAdapter.toggleSelection(selectedDialog)
                var subtitle = ""
                if (dialogsAdapter.selectedItems.size != 1) {
                    subtitle = getString(R.string.dialogs_actionmode_subtitle, dialogsAdapter.selectedItems.size.toString())
                } else {
                    subtitle = getString(R.string.dialogs_actionmode_subtitle_single, dialogsAdapter.selectedItems.size.toString())
                }
                currentActionMode!!.subtitle = subtitle
                currentActionMode!!.menu.get(0).isVisible = (dialogsAdapter.selectedItems.size >= 1)
            } else if (ChatHelper.isLogged()) {
                showProgressDialog(R.string.dlg_loading)
                ChatActivity.startForResult(this, REQUEST_DIALOG_ID_FOR_UPDATE, selectedDialog)
            } else {
                showProgressDialog(R.string.dlg_login)
                ChatHelper.loginToChat(currentUser,
                        object : QBEntityCallback<Void> {
                            override fun onSuccess(p0: Void?, p1: Bundle?) {
                                hideProgressDialog()
                                ChatActivity.startForResult(this@DialogsActivity, REQUEST_DIALOG_ID_FOR_UPDATE, selectedDialog)
                            }

                            override fun onError(e: QBResponseException?) {
                                hideProgressDialog()
                                shortToast(R.string.login_chat_login_error)
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

        refreshLayout.setOnRefreshListener {
            cancelTasks()
            loadDialogsFromQb(silentUpdate = true, clearDialogHolder = true)
        }
        refreshLayout.setColorSchemeResources(R.color.color_new_blue, R.color.random_color_2, R.color.random_color_3, R.color.random_color_7)
    }

    private fun createDialog(selectedUsers: ArrayList<QBUser>, chatName: String) {
        ChatHelper.createDialogWithSelectedUsers(selectedUsers, chatName,
                object : QBEntityCallback<QBChatDialog> {
                    override fun onSuccess(dialog: QBChatDialog, args: Bundle?) {
                        Log.d(TAG, "Creating Dialog Successful")
                        isProcessingResultInProgress = false
                        dialogsManager.sendSystemMessageAboutCreatingDialog(systemMessagesManager, dialog)
                        val dialogs = ArrayList<QBChatDialog>()
                        dialogs.add(dialog)
                        QbDialogHolder.addDialogs(dialogs)
                        DialogJoinerAsyncTask(this@DialogsActivity, dialogs).execute()
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
        if (silentUpdate) {
            progress.visibility = View.VISIBLE
        } else {
            showProgressDialog(R.string.dlg_loading)
        }

        val requestBuilder = QBRequestGetBuilder()
        requestBuilder.limit = DIALOGS_PER_PAGE
        requestBuilder.skip = if (clearDialogHolder) {
            0
        } else {
            QbDialogHolder.dialogsMap.size
        }

        ChatHelper.getDialogs(requestBuilder, object : QBEntityCallback<ArrayList<QBChatDialog>> {
            override fun onSuccess(dialogs: ArrayList<QBChatDialog>, bundle: Bundle?) {
                if (dialogs.size < DIALOGS_PER_PAGE) {
                    hasMoreDialogs = false
                }
                if (clearDialogHolder) {
                    QbDialogHolder.clear()
                    hasMoreDialogs = true
                }
                QbDialogHolder.addDialogs(dialogs)
                updateDialogsAdapter()

                val joinerTask = DialogJoinerAsyncTask(this@DialogsActivity, dialogs)
                joinerTasksSet.add(joinerTask)
                joinerTask.execute()

                disableProgress()
                if (hasMoreDialogs) {
                    loadDialogsFromQb(true, false)
                }
            }

            override fun onError(e: QBResponseException) {
                disableProgress()
                shortToast(e.message)
            }
        })
    }

    private fun disableProgress() {
        isProcessingResultInProgress = false
        hideProgressDialog()
        refreshLayout.isRefreshing = false
        progress.visibility = View.GONE
    }

    private fun initConnectionListener() {
        val rootView: View = findViewById(R.id.layout_root)
        chatConnectionListener = object : VerboseQbChatConnectionListener(rootView) {
            override fun reconnectionSuccessful() {
                super.reconnectionSuccessful()
                loadDialogsFromQb(false, true)
            }
        }
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

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(80)

            mode.title = getString(R.string.dialogs_actionmode_title)
            mode.subtitle = getString(R.string.dialogs_actionmode_subtitle_single, "1")

            mode.menuInflater.inflate(R.menu.menu_activity_dialogs_action_mode, menu)
            val menuItem = menu.findItem(R.id.menu_dialogs_action_delete)
            if (menuItem != null && menuItem is TextView) {
            }
            dialogsAdapter.prepareToSelect()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_dialogs_action_delete -> {
                    deleteSelected()
                    currentActionMode?.finish()
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            currentActionMode = null
            dialogsAdapter.clearSelection()
        }

        private fun deleteSelected() {
            showProgressDialog(R.string.dlg_deleting_chats)
            val selectedDialogs = dialogsAdapter.selectedItems
            val groupDialogsToDelete = ArrayList<QBChatDialog>()
            val privateDialogsToDelete = ArrayList<QBChatDialog>()

            for (dialog in selectedDialogs) {
                if (dialog.type == QBDialogType.PUBLIC_GROUP) {
                    shortToast(getString(R.string.dialogs_cannot_delete_chat, dialog.name))
                } else if (dialog.type == QBDialogType.GROUP) {
                    groupDialogsToDelete.add(dialog)
                } else if (dialog.type == QBDialogType.PRIVATE) {
                    privateDialogsToDelete.add(dialog)
                }
            }

            if (privateDialogsToDelete.size > 0) {
                deletePrivateDialogs(privateDialogsToDelete)
            }

            if (groupDialogsToDelete.size > 0) {
                notifyDialogsLeave(groupDialogsToDelete)
                leaveGroupDialogs(groupDialogsToDelete)
            } else {
                hideProgressDialog()
            }
        }

        private fun deletePrivateDialogs(privateDialogsToDelete: List<QBChatDialog>) {
            ChatHelper.deletePrivateDialogs(privateDialogsToDelete, object : QBEntityCallback<ArrayList<String>> {
                override fun onSuccess(dialogsIds: ArrayList<String>, bundle: Bundle?) {
                    Log.d(TAG, "PRIVATE Dialogs Deleting Successful")
                    QbDialogHolder.deleteDialogs(dialogsIds)
                    updateDialogsAdapter()
                }

                override fun onError(e: QBResponseException) {
                    Log.d(TAG, "Deleting PRIVATE Dialogs Error: " + e.message)
                    showErrorSnackbar(R.string.dialogs_deletion_error, e,
                            View.OnClickListener { deletePrivateDialogs(privateDialogsToDelete) })
                }
            })
        }

        private fun leaveGroupDialogs(groupDialogsToDelete: List<QBChatDialog>) {
            ChatHelper.leaveGroupDialogs(groupDialogsToDelete, object : QBEntityCallback<List<QBChatDialog>> {
                override fun onSuccess(qbChatDialogs: List<QBChatDialog>, bundle: Bundle?) {
                    Log.d(TAG, "GROUP Dialogs Deleting Successful")
                    QbDialogHolder.deleteDialogs(qbChatDialogs)
                    updateDialogsAdapter()
                    hideProgressDialog()
                }

                override fun onError(e: QBResponseException) {
                    hideProgressDialog()
                    Log.d(TAG, "Deleting GROUP Dialogs Error: " + e.message)
                    longToast(R.string.dialogs_deletion_error)
                }
            })
        }

        private fun notifyDialogsLeave(dialogsToNotify: List<QBChatDialog>) {
            for (dialog in dialogsToNotify) {
                dialogsManager.sendMessageLeftUser(dialog)
                dialogsManager.sendSystemMessageLeftUser(systemMessagesManager, dialog)
                try {
                    // Its a hack to give the Chat Server more time to process the message and deliver them
                    Thread.sleep(300)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private inner class PushBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val message = intent.getStringExtra(EXTRA_FCM_MESSAGE)
            Log.v(TAG, "Received broadcast " + intent.action + " with data: " + message)
            loadDialogsFromQb(false, false)
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
            if (senderID != currentUser.id) {
                dialogsManager.onGlobalMessageReceived(dialogID, qbChatMessage)
            }
        }
    }

    private class DialogJoinerAsyncTask internal constructor(dialogsActivity: DialogsActivity,
                                                             private val dialogs: ArrayList<QBChatDialog>) : BaseAsyncTask<Void, Void, Void>() {
        private val activityRef: WeakReference<DialogsActivity> = WeakReference(dialogsActivity)

        @Throws(Exception::class)
        override fun performInBackground(vararg params: Void): Void? {
            if (!isCancelled) {
                ChatHelper.join(dialogs)
            }
            return null
        }

        override fun onResult(result: Void?) {
            if (!isCancelled && !activityRef.get()?.hasMoreDialogs!!) {
                activityRef.get()?.disableProgress()
            } else {
            }
        }

        override fun onException(e: Exception) {
            super.onException(e)
            if (!isCancelled) {
                Log.d("Dialog Joiner Task", "Error: $e")
                shortToast("Error: " + e.message)
            }
        }

        override fun onCancelled() {
            super.onCancelled()
            cancel(true)
        }
    }
}