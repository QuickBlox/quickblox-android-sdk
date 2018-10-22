package com.quickblox.sample.videochatkotlin

import android.app.Application
import com.quickblox.auth.session.QBSettings
import com.quickblox.core.ServiceZone
import com.quickblox.sample.videochatkotlin.utils.SAMPLE_CONFIG_FILE_NAME
import com.quickblox.sample.videochatkotlin.utils.getAllUsersFromFile
import com.quickblox.users.model.QBUser

class App : Application() {
    private val applicationID = "72448"
    private val authKey = "f4HYBYdeqTZ7KNb"
    private val authSecret = "ZC7dK39bOjVc-Z8"
    private val accountKey = "C4_z7nuaANnBYmsG_k98"
    private val apiEndpoint = "https://api.quickblox.com"
    private val chatEndpoint = "chat.quickblox.com"

    override fun onCreate() {
        super.onCreate()
        checkQBConfigJson()
        checkUserJson()
        initCredentials()
    }

    private fun checkQBConfigJson() {
        if (applicationID.isEmpty() || authKey.isEmpty() || authSecret.isEmpty() || accountKey.isEmpty()
                || apiEndpoint.isEmpty() || chatEndpoint.isEmpty()) {
            throw AssertionError(getString(R.string.error_qb_credentials_empty))
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
        QBSettings.getInstance().setEndpoints(apiEndpoint, chatEndpoint, ServiceZone.PRODUCTION)
        QBSettings.getInstance().zone = ServiceZone.PRODUCTION
    }
}