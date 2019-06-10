package com.quickblox.sample.videochat.conference.kotlin

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.quickblox.auth.session.QBSettings
import com.quickblox.conference.ConferenceConfig
import com.quickblox.sample.videochat.conference.kotlin.db.DbHelper
import io.fabric.sdk.android.Fabric

//App credentials
private const val APPLICATION_ID = "72448"
private const val AUTH_KEY = "f4HYBYdeqTZ7KNb"
private const val AUTH_SECRET = "ZC7dK39bOjVc-Z8"
private const val ACCOUNT_KEY = "C4_z7nuaANnBYmsG_k98"
private const val JANUS_SERVER = ""

//Default user settings
const val USER_DEFAULT_PASSWORD = "x6Bt0VDy5"

class App : Application() {

    private lateinit var dbHelper: DbHelper

    companion object {
        private lateinit var instance: App

        @Synchronized
        fun getInstance(): App {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        dbHelper = DbHelper(this)
        initFabric()
        checkCredentials()
        initCredentials()
    }

    private fun initFabric() {
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
    }

    private fun checkCredentials() {
        if (APPLICATION_ID.isEmpty() || AUTH_KEY.isEmpty() || AUTH_SECRET.isEmpty() || ACCOUNT_KEY.isEmpty()
                || JANUS_SERVER.isEmpty()) {
            throw AssertionError(getString(R.string.error_qb_credentials_empty))
        }
    }

    private fun initCredentials() {
        QBSettings.getInstance().init(applicationContext, APPLICATION_ID, AUTH_KEY, AUTH_SECRET)
        QBSettings.getInstance().accountKey = ACCOUNT_KEY
        ConferenceConfig.setUrl(JANUS_SERVER)

        // Uncomment and put your Api and Chat servers endpoints if you want to point the sample
        // against your own server.
        //
        // QBSettings.getInstance().setEndpoints("https://your_api_endpoint.com", "your_chat_endpoint", ServiceZone.PRODUCTION);
        // QBSettings.getInstance().zone = ServiceZone.PRODUCTION
    }

    fun getDbHelper(): DbHelper {
        return dbHelper
    }
}