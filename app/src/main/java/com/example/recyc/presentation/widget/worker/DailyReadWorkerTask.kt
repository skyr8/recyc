package com.example.recyc.presentation.widget.worker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.recyc.R
import com.example.recyc.domain.usecase.GetCurrentDateUseCase
import com.example.recyc.domain.usecase.GetCurrentRecyclerDayUseCase
import com.example.recyc.domain.usecase.PreferenceUseCase
import com.example.recyc.presentation.widget.updateWidget
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

import android.util.Log
import com.example.recyc.domain.geofence.GeofenceActionReceiver
import com.example.recyc.domain.mapper.toIcon
import com.example.recyc.domain.model.RecyclingDayModel

@HiltWorker
internal class DailyReadWorkerTask @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workParams: WorkerParameters,
    private val getCurrentRecyclerDayUseCase: GetCurrentRecyclerDayUseCase,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
    private val preferenceUseCase: PreferenceUseCase,
) : CoroutineWorker(context, workParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        Log.d("DAILY_WORKER", "doWork: Started")
        val currentModel = getCurrentRecyclerDayUseCase()
        val recyclerJson = Gson().toJson(currentModel)
        val currentDate = getCurrentDateUseCase()
        val preferenceDate = preferenceUseCase.getLastNotificationDate()

        if (isOneHourBefore(currentModel?.hour) && (currentDate != preferenceDate)) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("DAILY_WORKER", "doWork: Showing notification")
                currentModel?.let { sendNotification(it) }
                preferenceUseCase.setLastNotificationDate(currentDate.orEmpty())
            }
        }

        return try {
            updateWidget(recyclerJson, context)
            Log.d("DAILY_WORKER", "doWork: Success")
            Result.success()
        } catch (e: Exception) {
            Log.e("DAILY_WORKER", "doWork: Error", e)
            Result.retry()
        }
    }

    private fun sendNotification(dayModel: RecyclingDayModel) {
        Log.d("LOCATION_SERVICE:::", "sendNotification (context) called")
        val channelId = "geofence_channel"
        val notificationId = 2

        val confirmIntent = Intent(context, GeofenceActionReceiver::class.java).apply {
            action = "com.example.recyc.ACTION_CONFIRM_DAY"
            putExtra("date", dayModel.day)
            putExtra("notificationId", notificationId)
        }

        val confirmPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        dayModel.type.first().toIcon().let { icon ->
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Do you recycle ${dayModel.type.joinToString(", ")} today?")
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

            val notification = builder.build()
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, notification)
            }
        }
    }


    private fun isOneHourBefore(recyclingHour: String?): Boolean {
        Log.d("DAILY_WORKER", "isOneHourBefore: Checking time")
        if (recyclingHour.isNullOrEmpty()) return false

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val recyclingTime = sdf.parse(recyclingHour) ?: return false

        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis

        val recyclingCalendar = Calendar.getInstance()
        recyclingCalendar.set(Calendar.HOUR_OF_DAY, recyclingTime.hours)
        recyclingCalendar.set(Calendar.MINUTE, recyclingTime.minutes)
        recyclingCalendar.set(Calendar.SECOND, 0)
        recyclingCalendar.set(Calendar.MILLISECOND, 0)

        val recyclingTimeInMillis = recyclingCalendar.timeInMillis
        val timeDifference = recyclingTimeInMillis - currentTime
        val minutesDifference = TimeUnit.MILLISECONDS.toMinutes(timeDifference)

        val result = minutesDifference in 0..60
        Log.d("DAILY_WORKER", "isOneHourBefore: Result = $result")
        return result
    }
}