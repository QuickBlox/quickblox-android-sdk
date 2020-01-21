package com.quickblox.sample.chat.kotlin.ui.activity

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.users.model.QBUser

private const val SPLASH_DELAY = 1500

class SplashActivity : BaseActivity() {
    private val TAG = SplashActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        fillVersion()
        Handler().postDelayed({
            if (SharedPrefsHelper.hasQbUser()) {
                restoreChatSession()
            } else {
                LoginActivity.start(this)
                finish()
            }
        }, SPLASH_DELAY.toLong())
    }

    override fun onBackPressed() {

    }

    private fun fillVersion() {
        val appName = getString(R.string.app_name)
        findViewById<TextView>(R.id.tv_splash_app_title).text = appName
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        findViewById<TextView>(R.id.tv_splash_app_version).text = getString(R.string.splash_app_version, versionName)
    }

    private fun restoreChatSession() {
        if (ChatHelper.isLogged()) {
            DialogsActivity.start(this)
            finish()
        } else {
            val currentUser = getUserFromSession()
            if (currentUser == null) {
                LoginActivity.start(this)
                finish()
            } else {
                loginToChat(currentUser)
            }
        }
    }

    private fun getUserFromSession(): QBUser? {
        val user = SharedPrefsHelper.getQbUser()
        val qbSessionManager = QBSessionManager.getInstance()
        qbSessionManager.sessionParameters?.let {
            val userId = qbSessionManager.sessionParameters.userId
            user?.id = userId
            return user
        } ?: run {
            ChatHelper.destroy()
            return null
        }
    }

    private fun loginToChat(user: QBUser) {
        showProgressDialog(R.string.dlg_restoring_chat_session)
        ChatHelper.loginToChat(user, object : QBEntityCallback<Void> {
            override fun onSuccess(result: Void?, bundle: Bundle?) {
                Log.v(TAG, "Chat login onSuccess()")
                hideProgressDialog()
                DialogsActivity.start(this@SplashActivity)
                finish()
            }

            override fun onError(e: QBResponseException) {
                if (e.message.equals("You have already logged in chat")) {
                    loginToChat(user)
                } else {
                    hideProgressDialog()
                    Log.w(TAG, "Chat login onError(): $e")
                    showErrorSnackbar(R.string.error_recreate_session, e, View.OnClickListener { loginToChat(user) })
                }
            }
        })
    }
}