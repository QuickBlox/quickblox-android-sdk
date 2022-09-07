package com.quickblox.sample.videochat.kotlin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.videochat.kotlin.DEFAULT_USER_PASSWORD
import com.quickblox.sample.videochat.kotlin.R
import com.quickblox.sample.videochat.kotlin.services.LoginService
import com.quickblox.sample.videochat.kotlin.utils.*
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser

const val ERROR_LOGIN_ALREADY_TAKEN_HTTP_STATUS = 422

class LoginActivity : BaseActivity() {
    private lateinit var userLoginEditText: EditText
    private lateinit var userDisplayNameEditText: EditText

    private lateinit var user: QBUser

    companion object {
        fun start(context: Context) =
            context.startActivity(Intent(context, LoginActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initUI()
    }

    private fun initUI() {
        supportActionBar?.title = getString(R.string.title_login_activity)

        userLoginEditText = findViewById(R.id.userLoginEditText)
        userLoginEditText.addTextChangedListener(LoginEditTextWatcher(userLoginEditText))

        userDisplayNameEditText = findViewById(R.id.userDisplayNameEditText)
        userDisplayNameEditText.addTextChangedListener(LoginEditTextWatcher(userDisplayNameEditText))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_login, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_login_user_done -> {
                val isNotValidLogin = !isLoginValid(userLoginEditText.text.toString())
                if (isNotValidLogin) {
                    userLoginEditText.error = getString(R.string.error_login)
                    return false
                }

                val isNotValidDisplayName = !isDisplayNameValid(userDisplayNameEditText.text.toString())
                if (isNotValidDisplayName) {
                    userDisplayNameEditText.error = getString(R.string.error_display_name)
                    return false
                }
                hideKeyboard(userDisplayNameEditText)
                val user = createUser()
                signUp(user)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signUp(user: QBUser) {
        showProgressDialog(R.string.dlg_creating_new_user)
        QBUsers.signUp(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(result: QBUser, params: Bundle) {
                SharedPrefsHelper.saveCurrentUser(user)
                loginToChat(result)
            }

            override fun onError(e: QBResponseException) {
                if (e.httpStatusCode == ERROR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                    loginToRest(user)
                } else {
                    hideProgressDialog()
                    longToast(R.string.sign_up_error)
                }
            }
        })
    }

    private fun loginToChat(user: QBUser) {
        user.password = DEFAULT_USER_PASSWORD
        this.user = user
        startLoginService(user)
    }

    private fun createUser(): QBUser {
        val user = QBUser()
        val userLogin = userLoginEditText.text.toString()
        val userFullName = userDisplayNameEditText.text.toString()
        user.login = userLogin
        user.fullName = userFullName
        user.password = DEFAULT_USER_PASSWORD
        return user
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == EXTRA_LOGIN_RESULT_CODE) {
            hideProgressDialog()

            var isLoginSuccessInChat = false
            data?.let {
                isLoginSuccessInChat = it.getBooleanExtra(EXTRA_LOGIN_RESULT, false)
            }

            if (isLoginSuccessInChat) {
                SharedPrefsHelper.saveCurrentUser(user)
                loginToRest(user)
            } else {
                var errorMessage: String? = getString(R.string.unknown_error)
                data?.let {
                    errorMessage = it.getStringExtra(EXTRA_LOGIN_ERROR_MESSAGE)
                }
                longToast(getString(R.string.login_chat_login_error) + errorMessage)
                userLoginEditText.setText(user.login)
                userDisplayNameEditText.setText(user.fullName)
            }
        }
    }

    private fun loginToRest(user: QBUser) {
        QBUsers.signIn(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(result: QBUser, params: Bundle) {
                SharedPrefsHelper.saveCurrentUser(user)
                updateUserOnServer(user)
            }

            override fun onError(responseException: QBResponseException) {
                hideProgressDialog()
                longToast(R.string.sign_in_error)
            }
        })
    }

    private fun updateUserOnServer(user: QBUser) {
        user.password = null
        QBUsers.updateUser(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(user: QBUser?, params: Bundle?) {
                hideProgressDialog()
                OpponentsActivity.start(this@LoginActivity)
                finish()
            }

            override fun onError(responseException: QBResponseException?) {
                hideProgressDialog()
                longToast(R.string.update_user_error)
            }
        })
    }

    override fun onBackPressed() {
        finish()
    }

    private fun startLoginService(qbUser: QBUser) {
        val tempIntent = Intent(this, LoginService::class.java)
        val pendingIntent = createPendingResult(EXTRA_LOGIN_RESULT_CODE, tempIntent, 0)
        LoginService.loginToChatAndInitRTCClient(this, qbUser, pendingIntent)
    }

    private inner class LoginEditTextWatcher internal constructor(private val editText: EditText) :
        TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            editText.error = null
        }

        override fun afterTextChanged(s: Editable) {

        }
    }
}