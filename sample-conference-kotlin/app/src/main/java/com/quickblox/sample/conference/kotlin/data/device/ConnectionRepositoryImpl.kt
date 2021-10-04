package com.quickblox.sample.conference.kotlin.data.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.quickblox.sample.conference.kotlin.domain.repositories.connection.ConnectionRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.connection.ConnectivityChangedListener

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class ConnectionRepositoryImpl(context: Context) : ConnectionRepository {
    private val listeners = HashSet<ConnectivityChangedListener>()
    private val networkCallback = NetworkCallbackImpl()
    private val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        val builder = NetworkRequest.Builder()
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        val networkRequest = builder.build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun isInternetAvailable(): Boolean {
        var result = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities)
                    ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        else -> false
                    }
                }
            }
        }

        return result
    }

    override fun addListener(listener: ConnectivityChangedListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: ConnectivityChangedListener) {
        listeners.remove(listener)
    }

    private inner class NetworkCallbackImpl : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
                for (listener in listeners) {
                    Handler(Looper.getMainLooper()).post {
                    listener.onAvailable()
                }
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
                for (listener in listeners) {
                    Handler(Looper.getMainLooper()).post {
                    listener.onLost()
                }
            }
        }
    }
}