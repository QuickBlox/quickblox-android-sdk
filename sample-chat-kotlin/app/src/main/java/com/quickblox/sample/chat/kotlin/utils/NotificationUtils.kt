package com.quickblox.sample.chat.kotlin.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

private const val CHANNEL_ONE_ID = "com.quickblox.samples.ONE"// The id of the channel.
private const val CHANNEL_ONE_NAME = "Channel One"

object NotificationUtils {

    fun showNotification(context: Context, activityClass: Class<out Activity>, title: String,
                         message: String, @DrawableRes icon: Int, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelIfNotExist(notificationManager)
        }
        val notification = buildNotification(context, activityClass, title, message, icon)
        notificationManager.notify(notificationId, notification)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannelIfNotExist(notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(CHANNEL_ONE_ID) == null) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.setShowBadge(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun buildNotification(context: Context, activityClass: Class<out Activity>,
                                  title: String, message: String, @DrawableRes icon: Int): Notification {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        return NotificationCompat.Builder(context, CHANNEL_ONE_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(buildContentIntent(context, activityClass, message))
                .build()
    }

    private fun buildContentIntent(context: Context, activityClass: Class<out Activity>, message: String): PendingIntent {
        val intent = Intent(context, activityClass)
        intent.putExtra(EXTRA_FCM_MESSAGE, message)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        var intentFlag = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intentFlag = PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getActivity(context, 0, intent, intentFlag)
    }
}