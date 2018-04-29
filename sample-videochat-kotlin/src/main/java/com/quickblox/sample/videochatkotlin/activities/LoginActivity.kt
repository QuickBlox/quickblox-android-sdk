package com.quickblox.sample.videochatkotlin.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.core.ui.activity.CoreBaseActivity
import com.quickblox.sample.core.utils.Toaster
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.utils.*
import com.quickblox.users.model.QBUser

/**
 * Created by Roman on 09.04.2018.
 */
class LoginActivity : CoreBaseActivity() {

    val TAG = LoginActivity::class.java.simpleName
    var progressDialog: ProgressDialog? = null
    lateinit var users: ArrayList<QBUser>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setActionBarTitle(R.string.title_login_activity)
        iniQBUsers()
        initButtons()
    }

    private fun iniQBUsers() {
        users = getAllUsersFromFile(SAMPLE_CONFIG_FILE_NAME)
    }

    private fun usersCount(): Int {
        return users.size
    }

    private fun initButtons() {//replace with adapter
        for (i in 1..usersCount()) {
            val myButton = Button(this)
            myButton.text = String.format(getString(R.string.user), i)
            myButton.setOnClickListener({
                Log.d(TAG, "users.get(i)= $i")
                loginToQB(users.get(i - 1))
//                myButton.isEnabled = false
            })

            val ll: LinearLayout = findViewById(R.id.button_layout)
            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            ll.addView(myButton, lp)
        }
    }

    private fun startCallActivity() {
        val intent = Intent(this, CallActivity::class.java)
        intent.putExtra(EXTRA_QB_USERS_LIST, users)
        startActivity(intent)
    }


    private fun loginToQB(user: QBUser) {
        showProgress(R.string.dlg_login)
        ChatHelper.instance.login(user, object : QBEntityCallback<Void> {
            override fun onSuccess(void:Void?, p1: Bundle?) {
                hideProgress()
                startCallActivity()
            }

            override fun onError(ex: QBResponseException) {
                hideProgress()
                Toaster.longToast(getString(R.string.login_chat_login_error) + ex.message)
            }
        })
    }

    fun showProgress(@StringRes messageId: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
        }
        showProgressDialog(this, progressDialog!!, messageId)
    }

    fun hideProgress() {
        if (progressDialog != null) {
            hideProgressDialog(progressDialog!!)
        }
    }
}