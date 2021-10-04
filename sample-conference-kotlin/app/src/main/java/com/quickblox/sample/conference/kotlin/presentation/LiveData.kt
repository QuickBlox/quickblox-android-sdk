package com.quickblox.sample.conference.kotlin.presentation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class LiveData<T> {
    private var value: T? = null
    private val observers: HashMap<(T?) -> Unit, LiveDataLifecycleObserver> = HashMap()

    fun setValue(value: T?) {
        this.value = value

        for (lifecycleObserver in observers.values) {
            val owner = lifecycleObserver.owner
            if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
                notifyChange(lifecycleObserver)
        }
    }

    fun getValue(): T? {
        return value
    }

    fun observe(owner: LifecycleOwner, observer: (T?) -> Unit) {
        val lifecycleObserver = LiveDataLifecycleObserver(owner, observer)
        observers[observer] = lifecycleObserver
        owner.lifecycle.addObserver(lifecycleObserver)
    }

    fun removeObserver(observer: (T?) -> Unit) {
        val lifecycleObserver = observers.remove(observer)
        lifecycleObserver?.owner?.lifecycle?.removeObserver(lifecycleObserver)
    }

    private fun notifyChange(lifecycleObserver: LiveDataLifecycleObserver) {
        lifecycleObserver.observer.invoke(value)
        this.value = null
    }

    private inner class LiveDataLifecycleObserver(val owner: LifecycleOwner, val observer: (T?) -> Unit) : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        private fun onResumed() {
            notifyChange(this)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        private fun onDestroyed() {
            removeObserver(observer)
        }
    }
}