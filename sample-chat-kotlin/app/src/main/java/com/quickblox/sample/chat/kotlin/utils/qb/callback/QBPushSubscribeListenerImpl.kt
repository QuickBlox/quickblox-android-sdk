package com.quickblox.sample.chat.kotlin.utils.qb.callback

import android.util.Log
import com.quickblox.messages.services.QBPushManager


open class QBPushSubscribeListenerImpl : QBPushManager.QBSubscribeListener {

    override fun onSubscriptionCreated() {
        Log.d("Subscription Listener", "Subscription Created")
    }

    override fun onSubscriptionError(e: Exception, i: Int) {

    }

    override fun onSubscriptionDeleted(b: Boolean) {

    }
}