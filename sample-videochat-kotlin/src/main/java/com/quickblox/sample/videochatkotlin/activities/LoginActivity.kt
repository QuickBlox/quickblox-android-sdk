package com.quickblox.sample.videochatkotlin.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.core.ui.activity.CoreBaseActivity
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.utils.*
import com.quickblox.users.model.QBUser


/**
 * Created by Roman on 09.04.2018.
 */
class LoginActivity : CoreBaseActivity() {

    val TAG = LoginActivity::class.java.simpleName
    private lateinit var users: ArrayList<QBUser>
    private lateinit var adapter: ArrayAdapter<String>
    var progressDialog: ProgressDialog? = null
    private var opponents: ArrayList<QBUser>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setActionBarTitle(R.string.title_login_activity)
        initQBUsers()
        initUserAdapter()
    }

    private fun initQBUsers() {
        users = getAllUsersFromFile(SAMPLE_CONFIG_FILE_NAME)
    }

    private fun initUserAdapter() {
        val userList: ArrayList<String> = ArrayList(users.size)

        users.forEachIndexed { index, _ -> userList.add(String.format(getString(R.string.user), index + 1)) }
        adapter = ArrayAdapter(this, R.layout.list_item_user, userList)
        val listView = findViewById<ListView>(R.id.list_users)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            loginToQB(users[position])
        }
    }

    private fun startCallActivity() {
        val intent = Intent(this, CallActivity::class.java)
        intent.putExtra(EXTRA_QB_USERS_LIST, opponents)
        startActivity(intent)
    }


    private fun loginToQB(user: QBUser) {
        showProgress(R.string.dlg_login)
        ChatHelper.instance.login(user, object : QBEntityCallback<Void> {
            override fun onSuccess(void: Void?, p1: Bundle?) {
                hideProgress()
                loadUsers()
            }

            override fun onError(ex: QBResponseException) {
                hideProgress()
                showErrorSnackbar(findViewById(android.R.id.content), R.string.login_chat_login_error, ex, View.OnClickListener { loginToQB(user) })
            }
        })
    }


    fun loadUsers() {
        showProgress(R.string.dlg_loading_opponents)
        val logins = ArrayList<String>()
        users.forEach { logins.add(it.login) }
        loadUsersByLogins(logins, object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(qbUsers: ArrayList<QBUser>, p1: Bundle?) {
                hideProgress()
                opponents = qbUsers
                startCallActivity()
            }

            override fun onError(responseException: QBResponseException) {
                hideProgress()
                showErrorSnackbar(findViewById(android.R.id.content), R.string.loading_users_error, responseException, View.OnClickListener { loadUsers() })
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
        if (progressDialog != null) {
            hideProgressDialog(progressDialog!!)
        }
    }
}