package com.quickblox.sample.videochatkotlin.activities

import android.content.Intent
import android.os.Bundle
import com.quickblox.sample.core.ui.activity.CoreSplashActivity
import com.quickblox.sample.videochatkotlin.R

/**
 * Created by roman on 4/6/18.
 */
class SplashActivity : CoreSplashActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkConfigsWithSnackebarError()) {
            proceedToTheNextActivityWithDelay()
        }
    }

    override fun getAppName(): String {
        return getString(R.string.app_title)
    }

    override fun proceedToTheNextActivity() {
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
        finish()
    }
}