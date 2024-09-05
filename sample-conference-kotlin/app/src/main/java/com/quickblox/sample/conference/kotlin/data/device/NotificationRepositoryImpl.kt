package com.quickblox.sample.conference.kotlin.data.device

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.domain.repositories.notification.NotificationRepository
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManager
import com.quickblox.sample.conference.kotlin.presentation.screens.splash.SplashActivity

private const val CHANNEL_ONE_ID = "QuickBlox Conference Chat Channel"
private const val CHANNEL_ONE_NAME = "Conference Chat Channel"
private const val EXTRA_FCM_MESSAGE = "message"
private const val NOTIFICATION_ID = 1

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class NotificationRepositoryImpl(private val context: Context, private val resourcesManager: ResourcesManager) :
    NotificationRepository {
    override fun showNotification(message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelIfNotExist(notificationManager)
        }
        val notification = buildNotification(message)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannelIfNotExist(notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(CHANNEL_ONE_ID) == null) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.setShowBadge(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun buildNotification(message: String): Notification {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val title = resourcesManager.get().getString(R.string.chat_notification_title)
        return NotificationCompat.Builder(context, CHANNEL_ONE_ID)
                .setSmallIcon(R.drawable.ic_logo_vector)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(buildContentIntent(context, message))
                .build()
    }

    private fun buildContentIntent(context: Context, message: String): PendingIntent? {
        val intent = Intent(context, SplashActivity::class.java)
        intent.putExtra(EXTRA_FCM_MESSAGE, message)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        var intentFlag = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intentFlag = PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getActivity(context, 0, intent, intentFlag)
    }
}