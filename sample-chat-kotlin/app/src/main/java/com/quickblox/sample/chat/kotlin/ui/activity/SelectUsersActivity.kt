package com.quickblox.sample.chat.kotlin.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.GenericQueryRule
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.ui.adapter.CheckboxUsersAdapter
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.sample.chat.kotlin.utils.shortToast
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import java.util.concurrent.TimeUnit

private const val EXTRA_QB_DIALOG = "qb_dialog"
private const val ORDER_RULE = "order"
private const val ORDER_VALUE = "desc string updated_at"
const val EXTRA_QB_USERS = "qb_users"
const val MINIMUM_CHAT_OCCUPANTS_SIZE = 1
const val PRIVATE_CHAT_OCCUPANTS_SIZE = 2
const val EXTRA_CHAT_NAME = "chat_name"

class SelectUsersActivity : BaseActivity() {
    private val CLICK_DELAY = TimeUnit.SECONDS.toMillis(2)

    private lateinit var usersListView: ListView
    private lateinit var progressBar: ProgressBar

    private lateinit var usersAdapter: CheckboxUsersAdapter
    private lateinit var users: List<QBUser>
    private var lastClickTime = 0L
    private var qbChatDialog: QBChatDialog? = null
    private var chatName: String? = null

    companion object {
        /**
         * Start activity for picking users
         *
         * @param activity activity to return result
         * @param code     request code for onActivityResult() method
         *                 <p>
         *                 in onActivityResult there will be 'ArrayList<QBUser>' in the intent extras
         *                 which can be obtained with SelectPeopleActivity.EXTRA_QB_USERS key
         */
        fun startForResult(activity: Activity, code: Int, dialog: QBChatDialog?) {
            val intent = Intent(activity, SelectUsersActivity::class.java)
            intent.putExtra(EXTRA_QB_DIALOG, dialog)
            activity.startActivityForResult(intent, code)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_users)

        intent.getSerializableExtra(EXTRA_QB_DIALOG)?.let {
            qbChatDialog = it as QBChatDialog
        }

        initUi()
        loadUsersFromQb()
    }

    private fun initUi() {
        progressBar = findViewById(R.id.progress_select_users)
        usersListView = findViewById(R.id.list_select_users)

        val listHeader = LayoutInflater.from(this)
                .inflate(R.layout.include_list_hint_header, usersListView, false) as TextView
        listHeader.setText(R.string.select_users_list_hint)
        usersListView.addHeaderView(listHeader, null, false)

        val editingChat = intent.getSerializableExtra(EXTRA_QB_DIALOG) != null
        if (editingChat) {
            supportActionBar?.title = getString(R.string.select_users_edit_chat)
        } else {
            supportActionBar?.title = getString(R.string.select_users_create_chat)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_select_users, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (SystemClock.uptimeMillis() - lastClickTime < CLICK_DELAY) {
            return super.onOptionsItemSelected(item)
        }
        lastClickTime = SystemClock.uptimeMillis()

        when (item.itemId) {
            R.id.menu_select_people_action_done -> {
                if (usersAdapter.selectedUsers.size < MINIMUM_CHAT_OCCUPANTS_SIZE) {
                    shortToast(R.string.select_users_choose_users)
                } else {
                    if (qbChatDialog == null && usersAdapter.selectedUsers.size >= PRIVATE_CHAT_OCCUPANTS_SIZE) {
                        showChatNameDialog()
                    } else {
                        passResultToCallerActivity()
                    }
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showChatNameDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_enter_chat_name, null)
        dialogBuilder.setView(dialogView)
        val editTextGroupName: EditText = dialogView.findViewById(R.id.edittext_dialog_name)

        dialogBuilder.setTitle(R.string.dialog_enter_chat_name)
        dialogBuilder.setPositiveButton(R.string.dialog_OK) { dialog, which ->
            if (TextUtils.isEmpty(editTextGroupName.text)) {
                shortToast(R.string.dialog_enter_chat_name)
            } else {
                chatName = editTextGroupName.text.toString()
                passResultToCallerActivity()
                dialog.dismiss()
            }
        }

        dialogBuilder.setNegativeButton(R.string.dialog_Cancel) { dialog, which ->
            dialog.dismiss()
        }

        dialogBuilder.create().show()
    }

    private fun passResultToCallerActivity() {
        val intent = Intent()
        intent.putExtra(EXTRA_QB_USERS, usersAdapter.selectedUsers)
        if (!TextUtils.isEmpty(chatName)) {
            intent.putExtra(EXTRA_CHAT_NAME, chatName)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun loadUsersFromQb() {
        progressBar.visibility = View.VISIBLE

        val rules = ArrayList<GenericQueryRule>()
        rules.add(GenericQueryRule(ORDER_RULE, ORDER_VALUE))

        val qbPagedRequestBuilder = QBPagedRequestBuilder()
        qbPagedRequestBuilder.rules = rules
        qbPagedRequestBuilder.perPage = 100

        QBUsers.getUsers(qbPagedRequestBuilder).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(usersList: ArrayList<QBUser>, params: Bundle?) {
                users = usersList
                if (qbChatDialog != null) {
                    // update occupants list form server
                    getDialog()
                } else {
                    usersAdapter = CheckboxUsersAdapter(this@SelectUsersActivity, users)
                    updateUsersAdapter()
                }
            }

            override fun onError(e: QBResponseException) {
                progressBar.visibility = View.GONE
                showErrorSnackbar(R.string.select_users_get_users_error, e, View.OnClickListener { loadUsersFromQb() })
            }
        })
    }

    private fun getDialog() {
        val dialogID = qbChatDialog!!.dialogId
        ChatHelper.getDialogById(dialogID, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle?) {
                this@SelectUsersActivity.qbChatDialog = qbChatDialog
                loadUsersFromDialog(qbChatDialog.occupants)
            }

            override fun onError(e: QBResponseException) {
                progressBar.visibility = View.GONE
                showErrorSnackbar(R.string.select_users_get_dialog_error, e, View.OnClickListener { loadUsersFromQb() })
            }
        })
    }

    private fun updateUsersAdapter() {
        usersAdapter = CheckboxUsersAdapter(this, users)
        qbChatDialog?.let {
            val occupants = qbChatDialog?.occupants ?: return
            usersAdapter.addSelectedUsers(occupants)
        }
        usersListView.adapter = usersAdapter
        progressBar.visibility = View.GONE
    }

    private fun loadUsersFromDialog(userIdsList: List<Int>) {
        QBUsers.getUsersByIDs(userIdsList, null).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(qbUsers: ArrayList<QBUser>?, p1: Bundle?) {
                usersAdapter = CheckboxUsersAdapter(this@SelectUsersActivity, users)
                qbUsers?.let {
                    it.forEach { user ->
                        usersAdapter.addUserToUserList(user)
                    }
                }
                updateUsersAdapter();
            }

            override fun onError(e: QBResponseException?) {
                showErrorSnackbar(R.string.select_users_get_users_dialog_error, e, null)
                progressBar.visibility = View.GONE
            }
        })
    }
}