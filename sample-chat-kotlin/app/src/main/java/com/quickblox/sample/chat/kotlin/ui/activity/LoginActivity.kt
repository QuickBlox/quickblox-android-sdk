package com.quickblox.sample.chat.kotlin.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.USER_DEFAULT_PASSWORD
import com.quickblox.sample.chat.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.sample.chat.kotlin.utils.isFullNameValid
import com.quickblox.sample.chat.kotlin.utils.isLoginValid
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser

private const val UNAUTHORIZED = 401

class LoginActivity : BaseActivity() {

    private lateinit var loginEt: EditText
    private lateinit var usernamedEt: EditText

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, LoginActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginEt = findViewById(R.id.login)
        usernamedEt = findViewById(R.id.user_name)

        loginEt.addTextChangedListener(TextWatcherListener(loginEt))
        usernamedEt.addTextChangedListener(TextWatcherListener(usernamedEt))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_login, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_login_user_done -> {
                if (isLoginValid(this, loginEt) && isFullNameValid(this, usernamedEt)) {
                    val qbUser = QBUser()
                    qbUser.login = loginEt.text.toString().trim { it <= ' ' }
                    qbUser.fullName = usernamedEt.text.toString().trim { it <= ' ' }
                    qbUser.password = USER_DEFAULT_PASSWORD
                    signIn(qbUser)
                }
                invalidateOptionsMenu()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun signIn(user: QBUser) {
        showProgressDialog(R.string.dlg_login)
        ChatHelper.login(user, object : QBEntityCallback<QBUser> {
            override fun onSuccess(userFromRest: QBUser, bundle: Bundle?) {
                if (userFromRest.fullName == user.fullName) {
                    loginToChat(user)
                } else {
                    //Need to set password NULL, because server will update user only with NULL password
                    user.password = null
                    updateUser(user)
                }
            }

            override fun onError(e: QBResponseException) {
                if (e.httpStatusCode == UNAUTHORIZED) {
                    signUp(user)
                } else {
                    hideProgressDialog()
                    showErrorSnackbar(R.string.login_chat_login_error, e, View.OnClickListener { signIn(user) })
                }
            }
        })
    }

    private fun updateUser(user: QBUser) {
        ChatHelper.updateUser(user, object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser, bundle: Bundle?) {
                loginToChat(user)
            }

            override fun onError(e: QBResponseException) {
                hideProgressDialog()
                showErrorSnackbar(R.string.login_chat_login_error, e, null)
            }
        })
    }

    private fun loginToChat(user: QBUser) {
        //Need to set password, because the server will not register to chat without password
        user.password = USER_DEFAULT_PASSWORD
        ChatHelper.loginToChat(user, object : QBEntityCallback<Void> {
            override fun onSuccess(void: Void?, bundle: Bundle?) {
                SharedPrefsHelper.saveQbUser(user)
                DialogsActivity.start(this@LoginActivity)
                finish()
                hideProgressDialog()
            }

            override fun onError(e: QBResponseException) {
                hideProgressDialog()
                showErrorSnackbar(R.string.login_chat_login_error, e, null)
            }
        })
    }

    private fun signUp(user: QBUser) {
        SharedPrefsHelper.removeQbUser()
        QBUsers.signUp(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(p0: QBUser?, p1: Bundle?) {
                hideProgressDialog()
                signIn(user)
            }

            override fun onError(exception: QBResponseException?) {
                hideProgressDialog()
                showErrorSnackbar(R.string.login_sign_up_error, exception, null)
            }
        })
    }

    private inner class TextWatcherListener(private var editText: EditText) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            editText.error = null
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }
}