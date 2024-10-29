package com.example.recyc.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.recyc.R
import com.example.recyc.domain.receiver.GeofenceActionReceiver
import com.example.recyc.domain.mapper.toIcon
import com.example.recyc.domain.model.RecyclingDayModel

fun Context.showNotification(dayModel: RecyclingDayModel) {
    val channelId = "geofence_channel"
    val notificationId = 2

    val confirmIntent = Intent(this, GeofenceActionReceiver::class.java).apply {
        action = GeofenceActionReceiver.ACTION_CONFIRM_DAY
        putExtra(GeofenceActionReceiver.DATE, dayModel.day.name)
        putExtra(GeofenceActionReceiver.NOTIFICATION_ID, notificationId)
    }

    val dismissIntent = Intent(this, GeofenceActionReceiver::class.java).apply {
        action = GeofenceActionReceiver.ACTION_DISMISS_DAY
        putExtra(GeofenceActionReceiver.NOTIFICATION_ID, notificationId)
    }

    val confirmPendingIntent = PendingIntent.getBroadcast(
        this,
        0,
        confirmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val dismissPendingIntent = PendingIntent.getBroadcast(
        this,
        0,
        dismissIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    dayModel.type.first().toIcon().let { icon ->
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(icon)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Have you already taken out ${dayModel.type.joinToString(", ")}?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVibrate(longArrayOf(0, 500, 1000))
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Yes",
                confirmPendingIntent
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Skip",
                dismissPendingIntent
            )

        val notification = builder.build()
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, notification)
        }
    }
}