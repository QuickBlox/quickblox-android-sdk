package com.quickblox.sample.videochat.conference.kotlin.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.adapter.CheckboxUsersAdapter
import com.quickblox.sample.videochat.conference.kotlin.db.QbUsersDbManager
import com.quickblox.sample.videochat.conference.kotlin.util.loadDialogByID
import com.quickblox.sample.videochat.conference.kotlin.util.loadUsersByTag
import com.quickblox.sample.videochat.conference.kotlin.util.updateDialog
import com.quickblox.sample.videochat.conference.kotlin.utils.PREF_CURRENT_ROOM_NAME
import com.quickblox.sample.videochat.conference.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.videochat.conference.kotlin.utils.shortToast
import com.quickblox.users.model.QBUser
import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit


class SelectUsersActivity : BaseActivity() {

    private lateinit var currentUser: QBUser
    private lateinit var usersRecyclerView: RecyclerView

    private val clickDelay = TimeUnit.SECONDS.toMillis(2)
    private var lastClickTime = 0L
    private var dialog: QBChatDialog? = null

    private var usersAdapter: CheckboxUsersAdapter? = null

    companion object {
        private const val EXTRA_USERS_IDS_KEY = "users_ids_key"
        private const val EXTRA_DIALOG_KEY = "dialog_key"

        private const val MINIMUM_CHAT_OCCUPANTS_SIZE = 2

        const val RESULT_USERS_KEY = "result_users_key"

        fun startForResult(activity: Activity, code: Int) {
            activity.startActivityForResult(Intent(activity, SelectUsersActivity::class.java), code)
        }

        fun startForResult(fragment: Fragment, code: Int, dialog: QBChatDialog) {
            val intent = Intent(fragment.activity, SelectUsersActivity::class.java)
            intent.putExtra(EXTRA_DIALOG_KEY, dialog)
            fragment.startActivityForResult(intent, code)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_users)

        currentUser = SharedPrefsHelper.getQbUser()!!
        dialog = intent.getSerializableExtra(EXTRA_DIALOG_KEY) as QBChatDialog?

        initUI()
        initActionBar()
        initUsersAdapter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        if (SystemClock.uptimeMillis() - lastClickTime < clickDelay) {
            return super.onOptionsItemSelected(item)
        }

        lastClickTime = SystemClock.uptimeMillis()

        when (item.itemId) {
            R.id.menu_select_people_action_done -> {
                if (dialog != null) {
                    addOccupantsToDialog(dialog!!)
                } else if (usersAdapter != null) {
                    val users = usersAdapter?.getSelectedUsers()!!
                    if (users.size >= MINIMUM_CHAT_OCCUPANTS_SIZE) {
                        passResultToCallerActivity(null)
                    } else {
                        shortToast(R.string.select_users_choose_users)
                    }
                }
                return true
            }
            R.id.menu_refresh_users -> {
                if (dialog != null) {
                    updateDialogAndUsers(dialog!!)
                } else {
                    loadUsersFromQb()
                }
                return super.onOptionsItemSelected(item)
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_select_users, menu)
        return true
    }

    private fun initUI() {
        usersRecyclerView = findViewById(R.id.list_select_users)
    }

    private fun initActionBar() {
        val title = if (dialog != null) {
            getString(R.string.select_users_edit_dialog)
        } else {
            getString(R.string.select_users_create_dialog)
        }
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initUsersAdapter() {
        val users = QbUsersDbManager.getAllUsers()
        if (dialog != null) {
            updateDialogAndUsers(dialog!!)
            usersAdapter = CheckboxUsersAdapter(this, ArrayList(), currentUser)
        } else {
            usersAdapter = CheckboxUsersAdapter(this, users, currentUser)
        }
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = usersAdapter
    }

    private fun updateDialogAndUsers(dialog: QBChatDialog) {
        showProgressDialog(R.string.dlg_loading_dialogs_users)
        loadDialogByID(dialog.dialogId, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(dialog: QBChatDialog, params: Bundle) {
                this@SelectUsersActivity.dialog?.setOccupantsIds(dialog.occupants)
                loadUsersFromQb()
                hideProgressDialog()
            }

            override fun onError(responseException: QBResponseException) {
                hideProgressDialog()
                showErrorSnackbar(R.string.dlg_error_loading, responseException, View.OnClickListener { loadUsersFromQb() })
            }
        })
    }

    private fun addOccupantsToDialog(dialog: QBChatDialog) {
        showProgressDialog(R.string.dlg_updating_dialog)

        val users = usersAdapter?.getSelectedUsers()
        val usersArray = users?.toTypedArray()
        Log.d("SelectedUsersActivity", "usersArray= " + Arrays.toString(usersArray))

        updateDialog(dialog, usersArray as Array<QBUser>, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(dialog: QBChatDialog, params: Bundle) {
                hideProgressDialog()
                passResultToCallerActivity(dialog.occupants)
            }

            override fun onError(responseException: QBResponseException) {
                hideProgressDialog()
                showErrorSnackbar(R.string.dlg_updating_dialog, responseException, View.OnClickListener { addOccupantsToDialog(dialog) })
            }
        })
    }

    private fun passResultToCallerActivity(occupantsIds: List<Int>?) {
        val result = Intent()
        val selectedUsers = ArrayList(usersAdapter?.getSelectedUsers())
        result.putExtra(RESULT_USERS_KEY, selectedUsers)
        if (occupantsIds != null) {
            result.putExtra(EXTRA_USERS_IDS_KEY, occupantsIds as Serializable?)
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    private fun loadUsersFromQb() {
        showProgressDialog(R.string.dlg_loading_dialogs_users)
        val currentRoomName = SharedPrefsHelper.get<String>(PREF_CURRENT_ROOM_NAME)
        loadUsersByTag(currentRoomName, object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(users: ArrayList<QBUser>, params: Bundle) {
                QbUsersDbManager.saveAllUsers(users, true)
                if (dialog != null) {
                    users.remove(currentUser)
                    removeExistentOccupants(users, dialog!!)
                }
                usersAdapter?.updateUsers(users)
                hideProgressDialog()
            }

            override fun onError(responseException: QBResponseException) {
                hideProgressDialog()
                showErrorSnackbar(R.string.loading_users_error, responseException, View.OnClickListener { loadUsersFromQb() })
            }
        })
    }

    private fun removeExistentOccupants(users: MutableList<QBUser>, dialog: QBChatDialog) {
        val userIDs = dialog.occupants ?: return
        val i = users.iterator()
        while (i.hasNext()) {
            val user = i.next()
            for (userID in userIDs) {
                if (user.id == userID) {
                    Log.d("SelectedUsersActivity", "users.remove(user)= $user")
                    i.remove()
                }
            }
        }
    }
}