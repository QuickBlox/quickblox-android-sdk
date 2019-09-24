package com.quickblox.sample.videochat.kotlin.util

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import java.util.concurrent.CopyOnWriteArraySet


class NetworkConnectionChecker(context: Application) {

    private val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val listeners = CopyOnWriteArraySet<OnConnectivityChangedListener>()

    init {
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(NetworkStateReceiver(), intentFilter)
    }

    fun registerListener(listener: OnConnectivityChangedListener) {
        listeners.add(listener)
        listener.connectivityChanged(isConnectedNow())
    }

    fun unregisterListener(listener: OnConnectivityChangedListener) {
        listeners.remove(listener)
    }

    private fun isConnectedNow(): Boolean {
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private inner class NetworkStateReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val isConnectedNow = isConnectedNow()

            for (listener in listeners) {
                listener.connectivityChanged(isConnectedNow)
            }
        }
    }

    interface OnConnectivityChangedListener {
        fun connectivityChanged(availableNow: Boolean)
    }
}