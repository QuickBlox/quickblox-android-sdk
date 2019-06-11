package com.quickblox.sample.chat.kotlin.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.ui.adapter.UsersAdapter
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.sample.chat.kotlin.utils.qb.QbUsersHolder
import com.quickblox.sample.chat.kotlin.utils.shortToast
import com.quickblox.users.model.QBUser

private const val EXTRA_DIALOG = "extra_dialog"

class ChatInfoActivity : BaseActivity() {

    private lateinit var usersListView: ListView
    private lateinit var qbDialog: QBChatDialog

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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        usersListView = findViewById(R.id.list_chat_info_users)
        qbDialog = intent.getSerializableExtra(EXTRA_DIALOG) as QBChatDialog
        getDialog()
    }

    private fun getDialog() {
        val dialogID = qbDialog.dialogId
        ChatHelper.getDialogById(dialogID, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle?) {
                qbDialog = qbChatDialog
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
        val users = QbUsersHolder.getUsersByIds(userIds)
        val adapter = UsersAdapter(this, users as MutableList<QBUser>)
        usersListView.adapter = adapter
    }
}