package com.quickblox.sample.pushnotifications.kotlin.fcm

import android.util.Log
import com.quickblox.messages.services.fcm.QBFcmPushListenerService
import com.quickblox.sample.pushnotifications.kotlin.R
import com.quickblox.sample.pushnotifications.kotlin.activities.SplashActivity
import com.quickblox.sample.pushnotifications.kotlin.utils.ActivityLifecycle
import com.quickblox.sample.pushnotifications.kotlin.utils.showNotification

private const val NOTIFICATION_ID = 1

class PushListenerService : QBFcmPushListenerService() {
    private val TAG = PushListenerService::class.java.simpleName

    override fun sendPushMessage(data: MutableMap<Any?, Any?>?, from: String?, message: String?) {
        super.sendPushMessage(data, from, message)
        Log.v(TAG, "From: " + from)
        Log.v(TAG, "Message: " + message)

        if (ActivityLifecycle.isBackground()) {
            showNotification(this, SplashActivity::class.java,
                    getString(R.string.notification_title), message,
                    R.mipmap.ic_notification, NOTIFICATION_ID)
        }
    }
}