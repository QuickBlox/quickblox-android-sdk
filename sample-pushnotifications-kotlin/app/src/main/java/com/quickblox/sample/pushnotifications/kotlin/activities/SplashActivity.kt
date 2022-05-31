package com.quickblox.sample.pushnotifications.kotlin.activities

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import com.quickblox.sample.pushnotifications.kotlin.R
import com.quickblox.sample.pushnotifications.kotlin.utils.EXTRA_FCM_MESSAGE
import com.quickblox.sample.pushnotifications.kotlin.utils.SharedPrefsHelper

private const val SPLASH_DELAY = 1500

class SplashActivity : BaseActivity() {
    private val TAG = SplashActivity::class.java.simpleName

    private var message: String? = null

    private val packageInfo: PackageInfo
        get() {
            try {
                return packageManager.getPackageInfo(packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                throw RuntimeException("Could not get package name: $e")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        val extras = intent.extras
        if (extras != null) {
            message = intent.extras?.getString(EXTRA_FCM_MESSAGE)
        }
        fillUI()
        startNextScreen()
    }

    private fun fillUI() {
        val versionName = packageInfo.versionName
        val appNameTextView = findViewById<TextView>(R.id.text_splash_app_title)
        val versionTextView = findViewById<TextView>(R.id.text_splash_app_version)

        appNameTextView.text = getString(R.string.app_title)
        versionTextView.text = getString(R.string.splash_app_version, versionName)
    }

    private fun startNextScreen() {
        val qbUser = SharedPrefsHelper.getQbUser()

        if (qbUser != null) {
            MessagesActivity.start(this@SplashActivity, message)
            finish()
        } else {
            Handler().postDelayed({
                LoginActivity.start(this@SplashActivity, message)
                finish()
            }, SPLASH_DELAY.toLong())
        }
    }
}