package com.quickblox.sample.conference.kotlin.data.push

import android.content.Context
import android.util.Log
import com.quickblox.messages.services.QBPushManager
import com.quickblox.messages.services.QBPushManager.QBSubscribeListener
import com.quickblox.messages.services.SubscribeService
import com.quickblox.sample.conference.kotlin.domain.repositories.push.PushRepository

private val TAG: String = PushRepositoryImpl::class.java.simpleName

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class PushRepositoryImpl(private val context: Context) : PushRepository {
    override fun unsubscribe(unsubscribeCallback: () -> Unit) {
        QBPushManager.getInstance().addListener(SubscribeListenerImpl(TAG, unsubscribeCallback))
        SubscribeService.unSubscribeFromPushes(context)
    }

    override fun isSubscribed(): Boolean {
        return QBPushManager.getInstance().isSubscribedToPushes
    }

    private inner class SubscribeListenerImpl(val tag: String, val unsubscribeCallback: () -> Unit) : QBSubscribeListener {
        override fun onSubscriptionCreated() {
            Log.d(TAG, "Subscription Created")
        }

        override fun onSubscriptionError(exception: Exception, i: Int) {
            Log.d(TAG, "Subscription Error - " + exception.message)
        }

        override fun onSubscriptionDeleted(deleted: Boolean) {
            unsubscribeCallback.invoke()
            QBPushManager.getInstance().removeListener(this)
            Log.d(TAG, "Subscription Deleted")
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is SubscribeListenerImpl) {
                return false
            }
            return tag == other.tag
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }
}