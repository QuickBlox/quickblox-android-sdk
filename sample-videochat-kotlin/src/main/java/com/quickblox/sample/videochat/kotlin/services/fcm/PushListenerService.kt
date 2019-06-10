package com.quickblox.sample.videochat.kotlin.services.fcm

import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.quickblox.messages.services.fcm.QBFcmPushListenerService
import com.quickblox.sample.videochat.kotlin.services.LoginService
import com.quickblox.sample.videochat.kotlin.utils.SharedPrefsHelper
import com.quickblox.users.model.QBUser

class PushListenerService : QBFcmPushListenerService() {
    private val TAG = PushListenerService::class.java.simpleName

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        if (SharedPrefsHelper.hasQbUser()) {
            val qbUser: QBUser = SharedPrefsHelper.getQbUser()
            Log.d(TAG, "App has logged user" + qbUser.id)
            LoginService.start(this, qbUser)
        }
    }

    override fun sendPushMessage(data: MutableMap<Any?, Any?>?, from: String?, message: String?) {
        super.sendPushMessage(data, from, message)
        Log.v(TAG, "From: $from")
        Log.v(TAG, "Message: $message")
    }
}