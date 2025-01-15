/*
 * Created by Injoit on 17.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.example.android_ui_kit_sample

import android.app.Application
import com.quickblox.QBSDK
import com.quickblox.chat.QBChatService

// app credentials
private const val APPLICATION_ID = ""
private const val AUTH_KEY = ""
private const val AUTH_SECRET = ""
private const val ACCOUNT_KEY = ""

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        checkQBCredentials()
        initQBSdk()
        setChatConfiguration()
    }

    private fun checkQBCredentials() {
        if (APPLICATION_ID.isEmpty() || AUTH_KEY.isEmpty() || AUTH_SECRET.isEmpty() || ACCOUNT_KEY.isEmpty()) {
            throw AssertionError(getString(R.string.error_qb_credentials_empty))
        }
    }

    private fun initQBSdk() {
        QBSDK.init(applicationContext, APPLICATION_ID, AUTH_KEY, AUTH_SECRET, ACCOUNT_KEY)
    }

    private fun setChatConfiguration() {
        QBChatService.setConfigurationBuilder(buildChatConfig())
        QBChatService.setDefaultPacketReplyTimeout(10000)
        QBChatService.setDebugEnabled(true)
    }

    private fun buildChatConfig(): QBChatService.ConfigurationBuilder {
        val configurationBuilder = QBChatService.ConfigurationBuilder()
        configurationBuilder.socketTimeout = 300
        configurationBuilder.preferredResumptionTime = 500
        return configurationBuilder
    }
}