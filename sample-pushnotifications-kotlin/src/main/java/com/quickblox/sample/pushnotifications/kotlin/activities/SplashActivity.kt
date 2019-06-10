package com.quickblox.sample.pushnotifications.kotlin.activities

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.pushnotifications.kotlin.R
import com.quickblox.sample.pushnotifications.kotlin.USER_LOGIN
import com.quickblox.sample.pushnotifications.kotlin.USER_PASSWORD
import com.quickblox.sample.pushnotifications.kotlin.utils.EXTRA_GCM_MESSAGE
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser

private const val SPLASH_DELAY = 1500

class SplashActivity : BaseActivity() {
    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        fillVersion()
        intent.extras?.let {
            message = it.getString(EXTRA_GCM_MESSAGE)
        }
        signInQB()
    }

    private fun signInQB() {
        val qbUser = QBUser(USER_LOGIN, USER_PASSWORD)
        QBUsers.signIn(qbUser).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser, bundle: Bundle) {
                Handler().postDelayed({
                    MessagesActivity.start(this@SplashActivity, message)
                    finish()
                }, SPLASH_DELAY.toLong())
            }

            override fun onError(e: QBResponseException) {
                showErrorSnackbar(R.string.splash_create_session_error, e, View.OnClickListener {
                    signInQB()
                })
            }
        })
    }

    private fun fillVersion() {
        val appName = getString(R.string.app_name)
        findViewById<TextView>(R.id.text_splash_app_title).text = appName
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        findViewById<TextView>(R.id.text_splash_app_version).text = getString(R.string.splash_app_version, versionName)
    }
}