package com.quickblox.sample.conference.kotlin.data.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.domain.call.CallManager
import com.quickblox.sample.conference.kotlin.presentation.screens.call.CallActivity
import com.quickblox.sample.conference.kotlin.presentation.utils.Constants
import com.quickblox.videochat.webrtc.*
import com.quickblox.videochat.webrtc.AppRTCAudioManager.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable
import java.util.*
import javax.inject.Inject

private const val SERVICE_ID = 646
private const val CHANNEL_ID = "Quickblox Conference Channel"
private const val CHANNEL_NAME = "Quickblox Background Conference service"

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class CallService : Service() {
    private val callServiceBinder = CallServiceBinder()

    @Inject
    lateinit var callManager: CallManager

    companion object {
        private var running = false

        fun isRunning(): Boolean {
            return running
        }

        fun start(context: Context) {
            val intent = Intent(context, CallService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallService::class.java)
            context.stopService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(SERVICE_ID, notification)
        running = true
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        running = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        return callServiceBinder
    }

    private fun buildNotification(): Notification {
        val notifyIntent = Intent(this, CallActivity::class.java)
        val notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val notificationTitle = getString(R.string.notification_title)
        val notificationText = getString(R.string.notification_text, "")
        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle(notificationTitle)
        bigTextStyle.bigText(notificationText)
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(CHANNEL_ID, CHANNEL_NAME) else getString(R.string.app_name)
        val builder = NotificationCompat.Builder(this, channelId)
        builder.setStyle(bigTextStyle)
        builder.setContentTitle(notificationTitle)
        builder.setContentText(notificationText)
        builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.drawable.ic_logo_vector)
        builder.setSmallIcon(R.drawable.ic_logo_vector)
        val bitmapIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_logo_vector)
        builder.setLargeIcon(bitmapIcon)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.priority = NotificationManager.IMPORTANCE_LOW
        } else {
            builder.priority = Notification.PRIORITY_LOW
        }
        builder.setContentIntent(notifyPendingIntent)
        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        channel.lightColor = getColor(R.color.green)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        return channelId
    }

    inner class CallServiceBinder : Binder() {
        val service: CallService
            get() = this@CallService
    }
}