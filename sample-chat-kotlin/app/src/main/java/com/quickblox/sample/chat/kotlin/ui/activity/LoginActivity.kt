package com.quickblox.sample.chat.kotlin.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
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
import java.util.*


private const val UNAUTHORIZED = 401
private const val DRAFT_LOGIN = "draft_login"
private const val DRAFT_USERNAME = "draft_username"

class LoginActivity : BaseActivity() {

    private lateinit var loginEt: EditText
    private lateinit var usernameEt: EditText
    private lateinit var loginHint: TextView
    private lateinit var usernameHint: TextView
    private lateinit var btnLogin: TextView
    private lateinit var chbSave: CheckBox
    private lateinit var rootView: LinearLayout
    private lateinit var hidableHolder: LinearLayout

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, LoginActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initViews()
        prepareListeners()
        fillViews()
        defineFocusedBehavior()
    }

    private fun initViews() {
        supportActionBar?.elevation = 20f
        loginHint = findViewById(R.id.tv_login_hint)
        usernameHint = findViewById(R.id.tv_username_hint)
        btnLogin = findViewById(R.id.tv_btn_login)
        loginEt = findViewById(R.id.et_login)
        usernameEt = findViewById(R.id.et_user_name)
        chbSave = findViewById(R.id.chb_login_save)
        btnLogin = findViewById(R.id.tv_btn_login)
        hidableHolder = findViewById(R.id.ll_hidable_holder)
        rootView = findViewById(R.id.root_view_login_activity)
    }

    private fun fillViews() {
        if (!SharedPrefsHelper.get<String>(DRAFT_LOGIN).isNullOrEmpty()) {
            loginEt.setText(SharedPrefsHelper.get<String>(DRAFT_LOGIN))
        }
        if (!SharedPrefsHelper.get<String>(DRAFT_USERNAME).isNullOrEmpty()) {
            usernameEt.setText(SharedPrefsHelper.get<String>(DRAFT_USERNAME))
        }
        validateFields()
    }

    @SuppressLint("NewApi")
    private fun defineFocusedBehavior() {
        loginHint.visibility = View.GONE
        usernameHint.visibility = View.GONE

        loginEt.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                loginEt.translationZ = 10f
            } else {
                loginEt.translationZ = 0f
            }
            if (isLoginValid(this, loginEt)) {
                loginHint.visibility = View.GONE
            } else {
                loginHint.visibility = View.VISIBLE
            }
        }

        usernameEt.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                usernameEt.translationZ = 10f
            } else {
                usernameEt.translationZ = 0f
            }
            if (isFullNameValid(this, usernameEt)) {
                usernameHint.visibility = View.GONE
            } else {
                usernameHint.visibility = View.VISIBLE
            }
        }
    }

    private fun prepareListeners() {
        rootView.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(v: View?): Boolean {
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(80)

                if (hidableHolder.visibility == View.GONE) {
                    hidableHolder.visibility = View.VISIBLE
                } else {
                    hidableHolder.visibility = View.GONE
                }
                return true
            }
        })

        btnLogin.setOnClickListener {
            if (btnLogin.isActivated) {
                showProgressDialog(R.string.dlg_login)
                prepareUser()
            }
        }

        loginEt.addTextChangedListener(TextWatcherListener(loginEt))
        usernameEt.addTextChangedListener(TextWatcherListener(usernameEt))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_login, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_login_app_info -> {
                AppInfoActivity.start(this)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun validateFields(): Boolean {
        if (isLoginValid(this@LoginActivity, loginEt)) {
            loginHint.visibility = View.GONE
        } else {
            loginHint.visibility = View.VISIBLE
        }

        if (isFullNameValid(this@LoginActivity, usernameEt)) {
            usernameHint.visibility = View.GONE
        } else {
            usernameHint.visibility = View.VISIBLE
        }

        if (isLoginValid(this@LoginActivity, loginEt) && isFullNameValid(this@LoginActivity, usernameEt)) {
            btnLogin.isActivated = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnLogin.elevation = 0F
                btnLogin.translationZ = 10F
            }
            return true
        } else {
            btnLogin.isActivated = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnLogin.elevation = 0F
                btnLogin.translationZ = 0F
            }
            return false
        }
    }

    private fun saveDrafts() {
        SharedPrefsHelper.save(DRAFT_LOGIN, loginEt.text.toString())
        SharedPrefsHelper.save(DRAFT_USERNAME, usernameEt.text.toString())
    }

    private fun clearDrafts() {
        SharedPrefsHelper.save(DRAFT_LOGIN, "")
        SharedPrefsHelper.save(DRAFT_USERNAME, "")
    }

    private fun prepareUser() {
        val qbUser = QBUser()
        qbUser.login = loginEt.text.toString().trim { it <= ' ' }
        qbUser.fullName = usernameEt.text.toString().trim { it <= ' ' }
        qbUser.password = USER_DEFAULT_PASSWORD
        signIn(qbUser)
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
                if (!chbSave.isChecked) {
                    clearDrafts()
                }
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

        private var timer = Timer()

        override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            val text = charSequence.toString().replace("  ", " ")
            if (editText.text.toString() != text) {
                editText.setText(text)
                editText.setSelection(text.length)
            }
            validateFields()
        }

        override fun afterTextChanged(s: Editable?) {
            timer.cancel()
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    saveDrafts()
                }
            }, 300)
        }
    }
}