package com.quickblox.sample.pushnotifications.kotlin

import android.app.Application
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.quickblox.auth.session.QBSession
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.auth.session.QBSessionParameters
import com.quickblox.auth.session.QBSettings
import com.quickblox.messages.services.QBPushManager
import com.quickblox.sample.pushnotifications.kotlin.utils.ActivityLifecycle
import com.quickblox.sample.pushnotifications.kotlin.utils.shortToast

// app credentials
private const val APPLICATION_ID = ""
private const val AUTH_KEY = ""
private const val AUTH_SECRET = ""
private const val ACCOUNT_KEY = ""

// default user config
const val DEFAULT_USER_PASSWORD = "quickblox"

class App : Application() {
    private val TAG = App::class.java.simpleName

    companion object {
        private lateinit var instance: App

        fun getInstance(): App = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        registerActivityLifecycleCallbacks(ActivityLifecycle)
        checkConfig()
        initCredentials()
        initQBSessionManager()
        initPushManager()
    }

    private fun checkConfig() {
        if (APPLICATION_ID.isEmpty() || AUTH_KEY.isEmpty() || AUTH_SECRET.isEmpty() || ACCOUNT_KEY.isEmpty()
                || DEFAULT_USER_PASSWORD.isEmpty()) {
            throw AssertionError(getString(R.string.error_qb_credentials_empty))
        }
    }

    private fun initCredentials() {
        QBSettings.getInstance().init(applicationContext, APPLICATION_ID, AUTH_KEY, AUTH_SECRET)
        QBSettings.getInstance().accountKey = ACCOUNT_KEY

        // uncomment and put your Api and Chat servers endpoints if you want to point the sample
        // against your own server.
        //
        // QBSettings.getInstance().setEndpoints("https://your_api_endpoint.com", "your_chat_endpoint", ServiceZone.PRODUCTION);
        // QBSettings.getInstance().zone = ServiceZone.PRODUCTION
    }

    private fun initQBSessionManager() {
        QBSessionManager.getInstance().addListener(object : QBSessionManager.QBSessionListener {
            override fun onSessionCreated(qbSession: QBSession) {
                Log.d(TAG, "Session Created")
            }

            override fun onSessionUpdated(qbSessionParameters: QBSessionParameters) {
                Log.d(TAG, "Session Updated")
            }

            override fun onSessionDeleted() {
                Log.d(TAG, "Session Deleted")
            }

            override fun onSessionRestored(qbSession: QBSession) {
                Log.d(TAG, "Session Restored")
            }

            override fun onSessionExpired() {
                Log.d(TAG, "Session Expired")
            }

            override fun onProviderSessionExpired(provider: String) {
                Log.d(TAG, "Session Expired for provider: $provider")
            }
        })
    }

    private fun initPushManager() {
        QBPushManager.getInstance().addListener(object : QBPushManager.QBSubscribeListener {
            override fun onSubscriptionCreated() {
                shortToast("Subscription Created")
                Log.d(TAG, "SubscriptionCreated")
            }

            override fun onSubscriptionError(e: Exception, resultCode: Int) {
                Log.d(TAG, "SubscriptionError" + e.localizedMessage)
                if (resultCode >= 0) {
                    val error = GoogleApiAvailability.getInstance().getErrorString(resultCode)
                    Log.d(TAG, "SubscriptionError playServicesAbility: $error")
                }
                shortToast(e.localizedMessage)
            }

            override fun onSubscriptionDeleted(success: Boolean) {

            }
        })
    }
}