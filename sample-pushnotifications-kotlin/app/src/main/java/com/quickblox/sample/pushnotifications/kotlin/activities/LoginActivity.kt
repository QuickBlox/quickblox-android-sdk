package com.quickblox.sample.pushnotifications.kotlin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.pushnotifications.kotlin.DEFAULT_USER_PASSWORD
import com.quickblox.sample.pushnotifications.kotlin.R
import com.quickblox.sample.pushnotifications.kotlin.utils.EXTRA_FCM_MESSAGE
import com.quickblox.sample.pushnotifications.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.pushnotifications.kotlin.utils.shortToast
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import java.util.regex.Pattern

private const val MAX_LOGIN_LENGTH = 50
private const val UNAUTHORIZED = 401

class LoginActivity : BaseActivity() {
    private val TAG = LoginActivity::class.java.simpleName

    private var buttonLogin: Button? = null
    private var editTextLogin: EditText? = null
    private var message: String? = null

    companion object {
        fun start(context: Context, message: String?) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(EXTRA_FCM_MESSAGE, message)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val extras = intent.extras
        if (extras != null) {
            message = intent.extras?.getString(EXTRA_FCM_MESSAGE)
        }

        buttonLogin = findViewById(R.id.btn_login)
        editTextLogin = findViewById(R.id.et_login)

        buttonLogin?.setOnClickListener {
            val patternLogin = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{2," + (MAX_LOGIN_LENGTH - 1) + "}+$")
            val matcherLogin = patternLogin.matcher(editTextLogin?.text.toString().trim { it <= ' ' })

            if (matcherLogin.matches()) {
                signIn()
            } else {
                shortToast(R.string.login_hint)
            }
        }
    }

    override fun onBackPressed() {
        // empty
    }

    private fun signIn() {
        showProgressDialog(R.string.dlg_sign_in)
        val login = editTextLogin?.text.toString().trim { it <= ' ' }
        val qbUser = QBUser(login, DEFAULT_USER_PASSWORD)
        qbUser.fullName = login

        QBUsers.signIn(qbUser).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(user: QBUser?, bundle: Bundle?) {
                Log.d(TAG, "SignIn Success: " + qbUser.id.toString())
                SharedPrefsHelper.saveQbUser(qbUser)
                hideProgressDialog()
                MessagesActivity.start(this@LoginActivity, message)
                finish()
            }

            override fun onError(exception: QBResponseException?) {
                Log.d(TAG, "SignIn Error: " + exception?.localizedMessage)
                if (exception?.httpStatusCode == UNAUTHORIZED) {
                    signUp(qbUser)
                } else {
                    exception?.let {
                        showErrorSnackbar(R.string.splash_signin_error, exception, View.OnClickListener {
                            signIn()
                        })
                    }
                }
            }
        })
    }

    private fun signUp(qbUser: QBUser) {
        showProgressDialog(R.string.dlg_sign_up)
        QBUsers.signUp(qbUser).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser?, bundle: Bundle?) {
                Log.d(TAG, "SignUp Success: " + qbUser?.id.toString())
                signIn()
            }

            override fun onError(exception: QBResponseException?) {
                Log.d(TAG, "SignUp Error: " + exception?.message)
                exception?.let {
                    showErrorSnackbar(R.string.splash_signup_error, exception, View.OnClickListener {
                        signIn()
                    })
                }
            }
        })
    }
}