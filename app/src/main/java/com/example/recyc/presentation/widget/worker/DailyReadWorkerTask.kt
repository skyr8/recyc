package com.example.recyc.presentation.widget.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.recyc.R
import com.example.recyc.domain.usecase.GetCurrentDateUseCase
import com.example.recyc.domain.usecase.GetCurrentDayUseCase
import com.example.recyc.domain.usecase.GetRecyclerUseCase
import com.example.recyc.domain.usecase.PreferenceUseCase
import com.example.recyc.presentation.widget.updateWidget
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@HiltWorker
internal class DailyReadWorkerTask @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workParams: WorkerParameters,
    private val getRecyclerUseCase: GetRecyclerUseCase,
    private val currentDayUseCase: GetCurrentDayUseCase,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
    private val preferenceUseCase: PreferenceUseCase,
) : CoroutineWorker(context, workParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val currentModel = getRecyclerUseCase().find { it.day == currentDayUseCase() }
        val recyclerJson = Gson().toJson(currentModel)
        val currentDate = getCurrentDateUseCase()
        val preferenceDate = preferenceUseCase.getLastNotificationDate()

        if (isOneHourBefore(currentModel?.hour) && (currentDate != preferenceDate)) {
            showNotification(currentModel?.type?.joinToString(", ").orEmpty())
            preferenceUseCase.setLastNotificationDate(currentDate.orEmpty())
        }

        return try {
            updateWidget(recyclerJson, context)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(recyclerType: String) {
        val channelId = "recycler_channel"
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(recyclerType)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    private fun isOneHourBefore(recyclingHour: String?): Boolean {
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

        return minutesDifference in 0..60
    }
}