package com.quickblox.sample.videochatkotlin

import com.quickblox.sample.core.CoreApp
import com.quickblox.sample.videochatkotlin.utils.SAMPLE_CONFIG_FILE_NAME
import com.quickblox.sample.videochatkotlin.utils.isConfigUserExist

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
        if (!isConfigUserExist(SAMPLE_CONFIG_FILE_NAME)) {
            val errorText = resources.openRawResource(R.raw.user_config_example_file)
                    .bufferedReader().use { it.readText() }
            throw AssertionError(String.format(getString(R.string.error_users_empty), errorText))
        }
    }
}