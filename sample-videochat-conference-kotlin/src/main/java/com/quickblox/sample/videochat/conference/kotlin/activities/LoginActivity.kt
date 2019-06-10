package com.quickblox.sample.videochat.conference.kotlin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.USER_DEFAULT_PASSWORD
import com.quickblox.sample.videochat.conference.kotlin.util.signInUser
import com.quickblox.sample.videochat.conference.kotlin.util.signUpNewUser
import com.quickblox.sample.videochat.conference.kotlin.util.updateUser
import com.quickblox.sample.videochat.conference.kotlin.utils.*
import com.quickblox.users.model.QBUser

private const val ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS = 422

class LoginActivity : BaseActivity() {
    private val TAG = LoginActivity::class.java.simpleName

    private lateinit var userNameEditText: EditText
    private lateinit var chatRoomNameEditText: EditText

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initUI()
    }

    private fun initUI() {
        supportActionBar?.title = getString(R.string.title_login_activity)

        userNameEditText = findViewById(R.id.user_name)
        userNameEditText.addTextChangedListener(LoginEditTextWatcher(userNameEditText))

        chatRoomNameEditText = findViewById(R.id.chat_room_name)
        chatRoomNameEditText.addTextChangedListener(LoginEditTextWatcher(chatRoomNameEditText))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_login, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_login_user_done -> {
                if (isEnteredUserNameValid() && isEnteredRoomNameValid()) {
                    hideKeyboard(userNameEditText)
                    hideKeyboard(chatRoomNameEditText)
                    signUp(createUserWithEnteredData())
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun isEnteredUserNameValid(): Boolean {
        return isUserNameValid(this, userNameEditText)
    }

    private fun isEnteredRoomNameValid(): Boolean {
        return isRoomNameValid(this, chatRoomNameEditText)
    }

    private fun createUserWithEnteredData(): QBUser {
        val qbUser = QBUser()
        val userName = userNameEditText.text.toString()
        val chatRoomName = chatRoomNameEditText.text.toString()
        val userTags = StringifyArrayList<String>()
        userTags.add(chatRoomName)
        qbUser.fullName = userName
        qbUser.login = userName
        qbUser.password = USER_DEFAULT_PASSWORD
        qbUser.tags = userTags
        return qbUser
    }

    private fun signUp(newUser: QBUser) {
        showProgressDialog(R.string.dlg_creating_new_user)
        signUpNewUser(newUser, object : QBEntityCallback<QBUser> {
            override fun onSuccess(result: QBUser, params: Bundle) {
                Log.d(TAG, "SignUp Successful")
                saveUserData(newUser)
                signIn(newUser)
            }

            override fun onError(e: QBResponseException) {
                if (e.httpStatusCode == ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                    signInCreatedUser(newUser)
                } else {
                    hideProgressDialog()
                    longToast(e.message ?: getString(R.string.sign_up_error))
                }
            }
        })
    }

    private fun signInCreatedUser(user: QBUser) {
        signInUser(user, object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser, params: Bundle) {
                Log.d(TAG, "SignIn Successful")
                val tags = StringifyArrayList<String>()
                tags.add(chatRoomNameEditText.text.toString())
                if (qbUser.tags == tags) {
                    qbUser.password = USER_DEFAULT_PASSWORD
                    saveUserData(qbUser)
                    hideProgressDialog()
                    DialogsActivity.start(this@LoginActivity)
                    finish()
                } else {
                    qbUser.tags = tags
                    updateUserOnServer(qbUser)
                }
            }

            override fun onError(responseException: QBResponseException) {
                hideProgressDialog()
                longToast(R.string.sign_up_error)
            }
        })
    }

    private fun updateUserOnServer(user: QBUser) {
        // Hack, because the server isn't update model QBUser with password != null;
        user.password = null

        updateUser(user, object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser, bundle: Bundle) {
                Log.d(TAG, "User Updated")
                user.password = USER_DEFAULT_PASSWORD
                saveUserData(user)
                hideProgressDialog()
                DialogsActivity.start(this@LoginActivity)
                finish()
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "Error Update User")
                showSnackbar(window.decorView.findViewById<View>(android.R.id.content),
                        R.string.sign_up_error, e, R.string.dlg_retry, View.OnClickListener { updateUserOnServer(user) })
            }
        })
    }

    private fun signIn(qbUser: QBUser) {
        qbUser.password = USER_DEFAULT_PASSWORD
        saveUserData(qbUser)
        signInCreatedUser(qbUser)
    }

    private fun saveUserData(qbUser: QBUser) {
        SharedPrefsHelper.save(PREF_CURRENT_ROOM_NAME, qbUser.tags[0])
        SharedPrefsHelper.saveQbUser(qbUser)
    }

    private inner class LoginEditTextWatcher internal constructor(private val editText: EditText) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            editText.error = null
        }

        override fun afterTextChanged(s: Editable) {

        }
    }
}