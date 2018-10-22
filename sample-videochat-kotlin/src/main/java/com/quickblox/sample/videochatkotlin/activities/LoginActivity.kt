package com.quickblox.sample.videochatkotlin.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.core.LogLevel
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.utils.EXTRA_QB_USERS_LIST
import com.quickblox.sample.videochatkotlin.utils.SAMPLE_CONFIG_FILE_NAME
import com.quickblox.sample.videochatkotlin.utils.getAllUsersFromFile
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import kotlinx.android.synthetic.main.activity_login.*

/**
 * Created by Roman on 09.04.2018.
 */
class LoginActivity : AppCompatActivity() {

    val TAG = LoginActivity::class.java.simpleName
    private lateinit var users: ArrayList<QBUser>
    private lateinit var adapter: ArrayAdapter<String>
    var progressDialog: ProgressDialog? = null
    private var opponents: ArrayList<QBUser>? = null
    private val qbChatService: QBChatService = QBChatService.getInstance()
    val isLoggedIn: Boolean
        get() = QBChatService.getInstance().isLoggedIn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.setTitle(R.string.title_login_activity)
        initChat()
        initQBUsers()
        initUserAdapter()
    }

    private fun initQBUsers() {
        users = getAllUsersFromFile(SAMPLE_CONFIG_FILE_NAME, this)
    }

    private fun initUserAdapter() {
        val userList: ArrayList<String> = ArrayList(users.size)
        users.forEachIndexed { index, _ -> userList.add(users[index].login) }
        adapter = ArrayAdapter(this, R.layout.list_item_user, userList)
        list_users.adapter = adapter
        list_users.choiceMode = ListView.CHOICE_MODE_SINGLE
        list_users.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            loginToQB(users[position])
        }
    }

    private fun initChat() {
        QBSettings.getInstance().logLevel = LogLevel.DEBUG
        QBChatService.setDebugEnabled(true)
        QBChatService.setConfigurationBuilder(QBChatService.ConfigurationBuilder().apply { socketTimeout = 0 })
    }

    private fun startCallActivity() {
        val intent = Intent(this, CallActivity::class.java)
        intent.putExtra(EXTRA_QB_USERS_LIST, opponents)
        startActivity(intent)
    }


    private fun loginToQB(user: QBUser) {
        showProgress(R.string.dlg_login)
        QBUsers.signIn(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser, args: Bundle) {
                user.id = qbUser.id!!
                loginToChat(user)
            }

            override fun onError(ex: QBResponseException) {
                hideProgress()
                Toast.makeText(applicationContext, getString(R.string.login_chat_login_error, ex.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun loginToChat(user: QBUser) {
        if (!isLoggedIn) {
            qbChatService.login(user, object : QBEntityCallback<Void> {
                override fun onSuccess(void: Void?, bundle: Bundle?) {
                    hideProgress()
                    loadUsers()
                }

                override fun onError(ex: QBResponseException) {
                    hideProgress()
                    Toast.makeText(applicationContext, getString(R.string.login_chat_login_error, ex.message), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun loadUsers() {
        showProgress(R.string.dlg_loading_opponents)
        val usersLogins = ArrayList<String>()
        users.forEach { usersLogins.add(it.login) }
        QBUsers.getUsersByLogins(usersLogins, null).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(qbUsers: ArrayList<QBUser>, p1: Bundle?) {
                hideProgress()
                opponents = qbUsers
                startCallActivity()
            }

            override fun onError(ex: QBResponseException) {
                hideProgress()
                Toast.makeText(applicationContext, getString(R.string.loading_users_error, ex.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showProgress(@StringRes messageId: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
        }
        showProgressDialog(this, progressDialog!!, messageId)
    }

    private fun hideProgress() {
        progressDialog?.dismiss()
    }

    private fun showProgressDialog(context: Context, progressDialog: ProgressDialog, @StringRes messageId: Int) {
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)

        progressDialog.setMessage(context.getString(messageId))

        progressDialog.show()
    }
}