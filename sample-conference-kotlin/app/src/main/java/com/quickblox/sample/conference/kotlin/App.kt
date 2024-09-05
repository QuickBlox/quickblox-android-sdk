package com.quickblox.sample.conference.kotlin

import android.app.Application
import android.text.TextUtils
import com.quickblox.auth.session.QBSettings
import com.quickblox.conference.ConferenceConfig
import com.quickblox.sample.conference.kotlin.domain.user.USER_DEFAULT_PASSWORD
import dagger.hilt.android.HiltAndroidApp

// chat settings range
private const val MAX_PORT_VALUE = 65535
private const val MIN_PORT_VALUE = 1000
private const val MIN_SOCKET_TIMEOUT = 300
private const val MAX_SOCKET_TIMEOUT = 60000
private const val CHAT_PORT = 5223
private const val SOCKET_TIMEOUT = 300

// app credentials
private const val APPLICATION_ID = ""
private const val AUTH_KEY = ""
private const val AUTH_SECRET = ""
private const val ACCOUNT_KEY = ""
private const val SERVER_URL = ""

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        checkAppCredentials()
        checkChatSettings()
        initCredentials()
        initConferenceConfig()
    }

    private fun checkAppCredentials() {
        if (APPLICATION_ID.isEmpty() || AUTH_KEY.isEmpty() || AUTH_SECRET.isEmpty() || ACCOUNT_KEY.isEmpty()) {
            throw AssertionError(getString(R.string.error_qb_credentials_empty))
        }
    }

    private fun checkChatSettings() {
        if (USER_DEFAULT_PASSWORD.isEmpty() || CHAT_PORT < MIN_PORT_VALUE || CHAT_PORT > MAX_PORT_VALUE
                || SOCKET_TIMEOUT < MIN_SOCKET_TIMEOUT || SOCKET_TIMEOUT > MAX_SOCKET_TIMEOUT) {
            throw AssertionError(getString(R.string.error_chat_credentials_empty))
        }
    }

    private fun initConferenceConfig() {
        if (TextUtils.isEmpty(SERVER_URL)) {
            throw AssertionError(getString(R.string.error_server_url_null))
        } else {
            ConferenceConfig.setUrl(SERVER_URL)
        }
    }

    private fun initCredentials() {
        QBSettings.getInstance().init(applicationContext, APPLICATION_ID, AUTH_KEY, AUTH_SECRET)
        QBSettings.getInstance().accountKey = ACCOUNT_KEY

        // uncomment and put your Api and Chat servers endpoints if you want to point the sample
        // against your own server.

        // QBSettings.getInstance().setEndpoints("https://your.api.endpoint.com", "your.chat.endpoint.com", ServiceZone.PRODUCTION);
        // QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
    }
}