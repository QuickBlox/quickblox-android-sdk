package com.quickblox.sample.chat.kotlin.utils.qb.callback

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException

open class QbEntityCallbackTwoTypeWrapper<T, R>(private var callback: QBEntityCallback<R>) : QBEntityCallback<T> {
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    override fun onSuccess(t: T, bundle: Bundle?) {
        // Do nothing, we want to trigger callback with another data type
    }

    override fun onError(error: QBResponseException) {
        onErrorInMainThread(error)
    }

    protected fun onSuccessInMainThread(result: R, bundle: Bundle?) {
        mainThreadHandler.post { callback.onSuccess(result, bundle) }
    }

    private fun onErrorInMainThread(error: QBResponseException) {
        mainThreadHandler.post { callback.onError(error) }
    }
}