package com.quickblox.sample.videochatkotlin

import android.app.Application
import android.text.TextUtils
import com.quickblox.auth.session.QBSettings
import com.quickblox.core.ServiceZone
import com.quickblox.sample.videochatkotlin.utils.SAMPLE_CONFIG_FILE_NAME
import com.quickblox.sample.videochatkotlin.utils.getAllUsersFromFile
import com.quickblox.users.model.QBUser

class App : Application() {
    private val applicationID = "29949"
    private val authKey = "9HTvhpTECXcfLfR"
    private val authSecret = "KBA8tPCPFpsXFCw"
    private val accountKey = "zKQrk7gkoqQsFhY1pNzv"

    override fun onCreate() {
        super.onCreate()
        checkQBConfigJson()
        checkUserJson()
        initCredentials()
    }

    private fun checkQBConfigJson() {
        if (applicationID.isEmpty() || authKey.isEmpty() || authSecret.isEmpty()) {
            val errorText = resources.openRawResource(R.raw.qb_config_example_file)
                    .bufferedReader().use { it.readText() }
            throw AssertionError(String.format(getString(R.string.error_qb_credentials_empty), errorText))
        }
    }

    private fun checkUserJson() {
        val users = getAllUsersFromFile(SAMPLE_CONFIG_FILE_NAME, this)
        if (users.size !in 2..4 || isUsersEmpty(users))
            throw AssertionError(getString(R.string.error_users_empty))
    }

    private fun isUsersEmpty(users: ArrayList<QBUser>): Boolean {
        users.forEach { user -> if (user.login.isBlank() || user.password.isBlank()) return true }
        return false
    }

    private fun initCredentials() {
        QBSettings.getInstance().init(applicationContext, applicationID, authKey, authSecret)
        QBSettings.getInstance().accountKey = accountKey
    }
}