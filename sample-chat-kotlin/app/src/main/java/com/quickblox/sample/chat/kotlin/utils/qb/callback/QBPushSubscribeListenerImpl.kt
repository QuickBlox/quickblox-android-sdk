package com.quickblox.sample.chat.kotlin.utils.qb.callback

import com.quickblox.messages.services.QBPushManager


open class QBPushSubscribeListenerImpl : QBPushManager.QBSubscribeListener {

    override fun onSubscriptionCreated() {

    }

    override fun onSubscriptionError(e: Exception, i: Int) {

    }

    override fun onSubscriptionDeleted(b: Boolean) {

    }
}