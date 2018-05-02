package com.quickblox.sample.videochatkotlin

import com.quickblox.sample.core.CoreApp
import com.quickblox.sample.videochatkotlin.utils.SAMPLE_CONFIG_FILE_NAME
import com.quickblox.sample.videochatkotlin.utils.getAllUsersFromFile
import com.quickblox.users.model.QBUser

class App : CoreApp() {

    override fun onCreate() {
        super.onCreate()
        checkQBConfigJson()
        checkUserJson()
    }

    private fun checkQBConfigJson() {
        if (qbConfigs.appId.isNullOrEmpty() || qbConfigs.authKey.isNullOrEmpty() || qbConfigs.authSecret.isNullOrEmpty()) {
            val errorText = resources.openRawResource(R.raw.qb_config_example_file)
                    .bufferedReader().use { it.readText() }
            throw AssertionError(String.format(getString(R.string.error_qb_credentials_empty), errorText))
        }
    }

    private fun checkUserJson() {
        val users = getAllUsersFromFile(SAMPLE_CONFIG_FILE_NAME)
        if (users.size !in 2..4 || isUsersEmpty(users))
            throw AssertionError(getString(R.string.error_users_empty))
    }

    private fun isUsersEmpty(users: ArrayList<QBUser>): Boolean {
        users.forEach { user -> if (user.login.isEmpty() || user.password.isEmpty()) return true }
        return false
    }
}