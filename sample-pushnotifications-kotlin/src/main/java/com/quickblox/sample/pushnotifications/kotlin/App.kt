package com.quickblox.sample.pushnotifications.kotlin

import android.app.Application
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.GoogleApiAvailability
import com.quickblox.auth.session.QBSession
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.auth.session.QBSessionParameters
import com.quickblox.auth.session.QBSettings
import com.quickblox.messages.services.QBPushManager
import com.quickblox.sample.pushnotifications.kotlin.utils.ActivityLifecycle
import com.quickblox.sample.pushnotifications.kotlin.utils.shortToast
import io.fabric.sdk.android.Fabric

//App Credentials
private const val APPLICATION_ID = "72448"
private const val AUTH_KEY = "f4HYBYdeqTZ7KNb"
private const val AUTH_SECRET = "ZC7dK39bOjVc-Z8"
private const val ACCOUNT_KEY = "C4_z7nuaANnBYmsG_k98"

//Default user config
const val USER_LOGIN = "test_user_id2"
const val USER_PASSWORD = "test_user_id2"

class App : Application() {

    private val TAG = App::class.java.simpleName

    companion object {
        lateinit var instanceApp: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instanceApp = this
        registerActivityLifecycleCallbacks(ActivityLifecycle)
        checkConfig()
        initCredentials()
        initQBSessionManager()
        initFabric()
        initPushManager()
    }

    private fun checkConfig() {
        if (APPLICATION_ID.isEmpty() || AUTH_KEY.isEmpty() || AUTH_SECRET.isEmpty() || ACCOUNT_KEY.isEmpty()
                || USER_LOGIN.isEmpty() || USER_PASSWORD.isEmpty()) {
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
                Log.d(TAG, "Session Expired for provider:" + provider)
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
                    Log.d(TAG, "SubscriptionError playServicesAbility: " + error)
                }
                shortToast(e.localizedMessage)
            }

            override fun onSubscriptionDeleted(success: Boolean) {

            }
        })
    }

    private fun initFabric() {
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
    }
}