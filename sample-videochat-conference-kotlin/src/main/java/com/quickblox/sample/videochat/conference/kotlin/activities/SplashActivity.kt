package com.quickblox.sample.videochat.conference.kotlin.activities

import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.utils.SharedPrefsHelper

class SplashActivity : BaseActivity() {

    companion object {
        private const val SPLASH_DELAY = 1500
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        fillVersion()
        Handler().postDelayed({
            if (SharedPrefsHelper.hasQbUser()) {
                DialogsActivity.start(this)
            } else {
                LoginActivity.start(this)
            }
            finish()
        }, SPLASH_DELAY.toLong())
    }

    private fun fillVersion() {
        val appName = getString(R.string.app_name)
        findViewById<TextView>(R.id.text_splash_app_title).text = appName
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        findViewById<TextView>(R.id.text_splash_app_version).text = getString(R.string.splash_app_version, versionName)
    }
}