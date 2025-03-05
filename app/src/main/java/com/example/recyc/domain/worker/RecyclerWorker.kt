package com.example.recyc.domain.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.recyc.R
import com.example.recyc.data.model.RecyclingType
import com.example.recyc.domain.RecyclerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@HiltWorker
class RecyclerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val recyclerRepository: RecyclerRepository
) : Worker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        val recyclerDays = runBlocking { recyclerRepository.getRecyclerDays() }
        val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()
        val todayRecycler = recyclerDays.find { it.day.name == today }

        todayRecycler?.let { todayRec ->
            if(todayRec.type.any { it != RecyclingType.NONE }){
                showNotification(todayRec.type.joinToString(", "))
            }
        }

        return Result.success()
    }

    private fun showNotification(recyclerType: String) {
        val channelId = "recycler_channel"
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.notification_channel_name)
            val descriptionText = applicationContext.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(recyclerType)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(notificationId, builder.build())
        }
    }
}