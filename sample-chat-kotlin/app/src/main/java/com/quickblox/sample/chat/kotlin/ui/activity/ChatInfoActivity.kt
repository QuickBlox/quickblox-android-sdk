package com.quickblox.sample.chat.kotlin.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import com.quickblox.chat.QBChatService
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.managers.DialogsManager
import com.quickblox.sample.chat.kotlin.ui.adapter.UsersAdapter
import com.quickblox.sample.chat.kotlin.ui.dialog.ProgressDialogFragment
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.sample.chat.kotlin.utils.qb.QbUsersHolder
import com.quickblox.sample.chat.kotlin.utils.shortToast
import com.quickblox.users.model.QBUser

private const val EXTRA_DIALOG = "extra_dialog"

class ChatInfoActivity : BaseActivity() {
    private val TAG = ChatInfoActivity::class.java.simpleName

    private lateinit var usersListView: ListView
    private lateinit var qbDialog: QBChatDialog
    private lateinit var userAdapter: UsersAdapter
    private var systemMessagesListener: SystemMessagesListener = SystemMessagesListener()
    private var dialogsManager: DialogsManager = DialogsManager()
    private var systemMessagesManager = QBChatService.getInstance().systemMessagesManager

    companion object {
        fun start(context: Context, qbDialog: QBChatDialog) {
            val intent = Intent(context, ChatInfoActivity::class.java)
            intent.putExtra(EXTRA_DIALOG, qbDialog)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_info)
        qbDialog = intent.getSerializableExtra(EXTRA_DIALOG) as QBChatDialog
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = qbDialog.name
        supportActionBar?.subtitle = getString(R.string.chat_info_subtitle, qbDialog.occupants.size.toString())
        usersListView = findViewById(R.id.list_chat_info_users)

        val userIds = qbDialog.occupants
        val users = QbUsersHolder.getUsersByIds(userIds)
        userAdapter = UsersAdapter(this, users as MutableList<QBUser>)
        usersListView.adapter = userAdapter
        getDialog()
    }

    override fun onStop() {
        super.onStop()
        systemMessagesManager.removeSystemMessageListener(systemMessagesListener)
    }

    override fun onResumeFinished() {
        systemMessagesManager.addSystemMessageListener(systemMessagesListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_chat_info, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_chat_info_action_add_people -> {
                updateDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun getDialog() {
        val dialogID = qbDialog.dialogId
        ChatHelper.getDialogById(dialogID, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle?) {
                qbDialog = qbChatDialog
                supportActionBar?.subtitle = getString(R.string.chat_info_subtitle, qbDialog.occupants.size.toString())
                buildUserList()
            }

            override fun onError(e: QBResponseException) {
                shortToast(e.message)
                finish()
            }
        })
    }

    private fun buildUserList() {
        val userIds = qbDialog.occupants
        if (QbUsersHolder.hasAllUsers(userIds)) {
            val users = QbUsersHolder.getUsersByIds(userIds)
            userAdapter.addUsers(users)
        } else {
            ChatHelper.getUsersFromDialog(qbDialog, object : QBEntityCallback<ArrayList<QBUser>> {
                override fun onSuccess(users: ArrayList<QBUser>?, b: Bundle?) {
                    users?.let {
                        QbUsersHolder.putUsers(it)
                        userAdapter.addUsers(users)
                    }
                }

                override fun onError(e: QBResponseException?) {
                    Log.d(TAG, e?.message)
                }
            })
        }
    }

    private fun updateDialog() {
        ProgressDialogFragment.show(supportFragmentManager)
        Log.d(TAG, "Starting Dialog Update")
        ChatHelper.getDialogById(qbDialog.dialogId, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(updatedChatDialog: QBChatDialog, bundle: Bundle) {
                Log.d(TAG, "Update Dialog Successful: " + updatedChatDialog.dialogId)
                qbDialog = updatedChatDialog
                ProgressDialogFragment.hide(supportFragmentManager)
                SelectUsersActivity.startForResult(this@ChatInfoActivity, REQUEST_CODE_SELECT_PEOPLE, updatedChatDialog)
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "Dialog Loading Error: " + e.message)
                ProgressDialogFragment.hide(supportFragmentManager)
                showErrorSnackbar(R.string.select_users_get_dialog_error, e, null)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult with resultCode: $resultCode requestCode: $requestCode")
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_PEOPLE && data != null) {
                showProgressDialog(R.string.chat_info_updating)
                val selectedUsers = data.getSerializableExtra(EXTRA_QB_USERS) as ArrayList<QBUser>
                val existingOccupants = qbDialog.occupants
                val newUserIds = ArrayList<Int>()

                for (user in selectedUsers) {
                    if (!existingOccupants.contains(user.id)) {
                        newUserIds.add(user.id)
                    }
                }

                ChatHelper.getDialogById(qbDialog.dialogId, object : QBEntityCallback<QBChatDialog> {
                    override fun onSuccess(qbChatDialog: QBChatDialog, p1: Bundle?) {
                        dialogsManager.sendMessageAddedUsers(qbChatDialog, newUserIds)
                        dialogsManager.sendSystemMessageAddedUser(systemMessagesManager, qbChatDialog, newUserIds)
                        qbChatDialog.let {
                            this@ChatInfoActivity.qbDialog = it
                        }
                        updateDialog(selectedUsers)
                    }

                    override fun onError(e: QBResponseException?) {
                        hideProgressDialog()
                        showErrorSnackbar(R.string.update_dialog_error, e, null)
                    }
                })
            }
        }
    }

    private fun updateDialog(selectedUsers: ArrayList<QBUser>) {
        ChatHelper.updateDialogUsers(qbDialog, selectedUsers, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(dialog: QBChatDialog, args: Bundle?) {
                qbDialog = dialog
                hideProgressDialog()
                finish()
            }

            override fun onError(e: QBResponseException) {
                showErrorSnackbar(R.string.chat_info_add_people_error, e, View.OnClickListener { updateDialog(selectedUsers) })
            }
        })
    }

    private inner class SystemMessagesListener : QBSystemMessageListener {
        override fun processMessage(qbChatMessage: QBChatMessage) {
            Log.d(TAG, "System Message Received: " + qbChatMessage.id)
            if (qbChatMessage.dialogId == qbDialog.dialogId) {
                getDialog()
            }
        }

        override fun processError(e: QBChatException?, qbChatMessage: QBChatMessage?) {
            Log.d(TAG, "System Messages Error: " + e?.message + "With MessageID: " + qbChatMessage?.id)
        }
    }
}