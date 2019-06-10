package com.quickblox.sample.videochat.kotlin

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.quickblox.auth.session.QBSettings
import com.quickblox.sample.videochat.kotlin.db.DbHelper
import io.fabric.sdk.android.Fabric

//User default credentials
const val DEFAULT_USER_PASSWORD = "quickblox"

//App credentials
private const val APPLICATION_ID = ""
private const val AUTH_KEY = ""
private const val AUTH_SECRET = ""
private const val ACCOUNT_KEY = ""

class App : Application() {

    private lateinit var dbHelper: DbHelper

    companion object {
        private lateinit var instance: App

        @Synchronized
        fun getInstance(): App = instance
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
        if (APPLICATION_ID.isEmpty() || AUTH_KEY.isEmpty() || AUTH_SECRET.isEmpty() || ACCOUNT_KEY.isEmpty()) {
            throw AssertionError(getString(R.string.error_qb_credentials_empty))
        }
    }

    private fun initCredentials() {
        QBSettings.getInstance().init(applicationContext, APPLICATION_ID, AUTH_KEY, AUTH_SECRET)
        QBSettings.getInstance().accountKey = ACCOUNT_KEY

        // Uncomment and put your Api and Chat servers endpoints if you want to point the sample
        // against your own server.
        //
        // QBSettings.getInstance().setEndpoints("https://your_api_endpoint.com", "your_chat_endpoint", ServiceZone.PRODUCTION);
        // QBSettings.getInstance().zone = ServiceZone.PRODUCTION
    }

    @Synchronized
    fun getDbHelper(): DbHelper {
        return dbHelper
    }
}