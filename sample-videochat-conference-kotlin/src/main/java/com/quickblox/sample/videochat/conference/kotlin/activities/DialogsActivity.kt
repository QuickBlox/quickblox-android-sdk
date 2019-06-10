package com.quickblox.sample.videochat.conference.kotlin.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.conference.ConferenceClient
import com.quickblox.conference.ConferenceSession
import com.quickblox.conference.WsException
import com.quickblox.conference.callbacks.ConferenceEntityCallback
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.adapter.DialogsAdapter
import com.quickblox.sample.videochat.conference.kotlin.db.QbUsersDbManager
import com.quickblox.sample.videochat.conference.kotlin.util.*
import com.quickblox.sample.videochat.conference.kotlin.utils.PREF_CURRENT_ROOM_NAME
import com.quickblox.sample.videochat.conference.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.videochat.conference.kotlin.utils.WebRtcSessionManager
import com.quickblox.sample.videochat.conference.kotlin.utils.shortToast
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCTypes

class DialogsActivity : BaseActivity() {
    private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

    private lateinit var dialogsRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var dialogID: String
    private lateinit var occupants: MutableList<Int>

    private var currentActionMode: ActionMode? = null
    private var dialogsAdapter: DialogsAdapter? = null
    private var currentUser: QBUser? = null
    private var isVideoCall: Boolean = false


    companion object {
        private const val PERMISSION_CODE = 177
        private const val REQUEST_USERS_CODE = 174

        fun start(context: Context) {
            context.startActivity(Intent(context, DialogsActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogs)

        currentUser = SharedPrefsHelper.getQbUser()

        initActionBar()
        initUI()
        startLoadDialogs()
        loadUsersFromQb()
    }

    private fun initActionBar() {
        val currentRoomName = SharedPrefsHelper[PREF_CURRENT_ROOM_NAME, ""]

        supportActionBar?.title = currentRoomName

        val currentUserFullName = currentUser?.fullName as String
        supportActionBar?.subtitle = String.format(getString(R.string.subtitle_text_logged_in_as), currentUserFullName)
    }

    override fun startSupportActionMode(callback: ActionMode.Callback): ActionMode? {
        currentActionMode = super.startSupportActionMode(callback) as ActionMode
        return currentActionMode
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val items = dialogsAdapter?.getSelectedItems()
        if (items != null && items.isNotEmpty()) {
            menuInflater.inflate(R.menu.activity_selected_opponents, menu)
        } else {
            menuInflater.inflate(R.menu.activity_opponents, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.update_opponents_list -> {
                startLoadDialogs()
                loadUsersFromQb()
                return true
            }
            R.id.settings -> {
                SettingsActivity.start(this)
                return true
            }
            R.id.log_out -> {
                removeAllUserData()
                return true
            }
            R.id.appinfo -> {
                AppInfoActivity.start(this)
                return true
            }
            R.id.start_video_call -> {
                isVideoCall = true
                startConference()
                return true
            }
            R.id.start_audio_call -> {
                isVideoCall = false
                startConference()
                return true
            }
            R.id.start_as_listener -> {
                isVideoCall = true
                startConference(dialogID, currentUser?.id as Int, isVideoCall, occupants, true)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_USERS_CODE) {
            val selectedUsers =
                    data?.getSerializableExtra(SelectUsersActivity.RESULT_USERS_KEY) as ArrayList<QBUser>
            showProgressDialog(R.string.dlg_create_dialog)
            createDialog(selectedUsers)
        }
        if (requestCode == PERMISSION_CODE) {
            val userId = currentUser?.id ?: return
            startConference(dialogID, userId, isVideoCall, occupants, false)
        }
    }

    private fun loadUsersFromQb() {
        showProgressDialog(R.string.dlg_loading_dialogs_users)
        val currentRoomName = SharedPrefsHelper.get<String>(PREF_CURRENT_ROOM_NAME)
        loadUsersByTag(currentRoomName, object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(result: ArrayList<QBUser>, params: Bundle) {
                hideProgressDialog()
                QbUsersDbManager.saveAllUsers(result, true)
            }

            override fun onError(responseException: QBResponseException) {
                hideProgressDialog()
                showErrorSnackbar(R.string.loading_users_error, responseException, View.OnClickListener { loadUsersFromQb() })
            }
        })
    }

    private fun removeAllUserData() {
        SharedPrefsHelper.clearAllData()
        QbUsersDbManager.clearDB()

        signOut()
        showLoginScreen()
    }

    private fun showLoginScreen() {
        LoginActivity.start(this)
        finish()
    }

    private fun initUI() {
        dialogsRecyclerView = findViewById(R.id.list_dialogs)
        fab = findViewById(R.id.fab_dialogs_new_chat)
        fab.setOnClickListener { SelectUsersActivity.startForResult(this, REQUEST_USERS_CODE) }
    }

    private fun startLoadDialogs() {
        showProgressDialog(R.string.dlg_loading_dialogs_users)
        loadDialogs(object : QBEntityCallback<ArrayList<QBChatDialog>> {
            override fun onSuccess(result: ArrayList<QBChatDialog>, params: Bundle) {
                hideProgressDialog()
                initDialogAdapter(result)
            }

            override fun onError(responseException: QBResponseException) {
                hideProgressDialog()
                showErrorSnackbar(R.string.loading_users_error, responseException, View.OnClickListener {
                    startLoadDialogs()
                })
            }
        })
    }

    private fun initDialogAdapter(chatDialogs: List<QBChatDialog>) {
        if (dialogsAdapter == null) {
            dialogsAdapter = DialogsAdapter(this, chatDialogs, object : DialogsAdapter.OnItemClickListener {
                override fun onShortClick(dialog: QBChatDialog) {
                    shortClickDialog(dialog)
                }

                override fun onLongClick(dialog: QBChatDialog) {
                    longClickDialog(dialog)
                }
            })
            dialogsRecyclerView.layoutManager = LinearLayoutManager(this)
            dialogsRecyclerView.adapter = dialogsAdapter
        } else {
            dialogsAdapter?.updateDialogs(chatDialogs)
        }
    }

    private fun shortClickDialog(dialog: QBChatDialog) {
        if (currentActionMode == null) {
            val occupants = dialog.occupants
            occupants.remove(currentUser?.id)
            dialogsAdapter?.toggleOneItem(dialog)
            invalidateOptionsMenu()
        } else {
            dialogsAdapter?.toggleSelection(dialog)
            updateActionBar(dialogsAdapter?.getSelectedItems()?.size)
        }
        occupants = dialog.occupants
        occupants.remove(currentUser?.id as Int)
        dialogID = dialog.dialogId
    }

    private fun longClickDialog(dialog: QBChatDialog) {
        fab.hide()
        startSupportActionMode(object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.delete_dialog -> {
                        deleteSelectedDialogs()
                        currentActionMode?.finish()
                        return true
                    }
                }
                return false
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.activity_selected_dialogs, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                currentActionMode = null
                dialogsAdapter?.clearSelectedItems()
                invalidateOptionsMenu()
                fab.show()
            }
        })
        dialogsAdapter?.selectItem(dialog)
        updateActionBar(dialogsAdapter?.getSelectedItems()?.size)
    }

    private fun updateActionBar(countSelectedUsers: Int?) {
        countSelectedUsers ?: return
        currentActionMode?.subtitle = null
        val title = if (countSelectedUsers > 1) {
            R.string.tile_many_dialogs_selected
        } else {
            R.string.title_one_dialog_selected
        }
        currentActionMode?.title = String.format(getString(title), countSelectedUsers)
        currentActionMode?.invalidate()
    }

    private fun deleteSelectedDialogs() {
        val selectedDialogs = dialogsAdapter?.getSelectedItems() ?: return
        deleteDialogs(selectedDialogs, object : QBEntityCallback<ArrayList<String>> {
            override fun onSuccess(dialogsIds: ArrayList<String>, bundle: Bundle) {
                startLoadDialogs()
            }

            override fun onError(e: QBResponseException) {
                showErrorSnackbar(R.string.dialogs_deletion_error, e,
                        View.OnClickListener { deleteSelectedDialogs() })
            }
        })
    }

    private fun createDialog(selectedUsers: ArrayList<QBUser>) {
        val currentUser = this.currentUser ?: return
        createDialogWithSelectedUsers(selectedUsers, currentUser, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(dialog: QBChatDialog, args: Bundle) {
                startLoadDialogs()
                hideProgressDialog()
            }

            override fun onError(e: QBResponseException) {
                hideProgressDialog()
                showErrorSnackbar(R.string.dialogs_creation_error, e, View.OnClickListener { })
            }
        }
        )
    }

    private fun startConference() {
        if (checkPermissions(*PERMISSIONS)) {
            PermissionsActivity.startForResult(this, PERMISSION_CODE, !isVideoCall, PERMISSIONS)
        } else {
            val userId = currentUser?.id ?: return
            startConference(dialogID, userId, isVideoCall, occupants, false)
        }
    }

    private fun startConference(dialogID: String, userID: Int, isVideoCall: Boolean, occupants: List<Int>, asListener: Boolean) {
        if (!NetworkConnectionChecker(application).isConnectedNow()) {
            shortToast(R.string.no_internet_connection)
            return
        }
        showProgressDialog(R.string.join_conference)
        val client = ConferenceClient.getInstance(applicationContext)
        val conferenceType = if (isVideoCall) {
            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
        } else {
            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO
        }
        client.createSession(userID, conferenceType, object : ConferenceEntityCallback<ConferenceSession> {
            override fun onSuccess(session: ConferenceSession) {
                hideProgressDialog()
                WebRtcSessionManager.setCurrentSession(session)
                CallActivity.start(this@DialogsActivity, dialogID, occupants, asListener)
            }

            override fun onError(responseException: WsException) {
                hideProgressDialog()
                showErrorSnackbar(R.string.join_conference_error, responseException, null)
            }
        })
    }
}