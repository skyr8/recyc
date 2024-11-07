package com.example.recyc.presentation.widget.worker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.recyc.domain.geofence.LocationService
import com.example.recyc.domain.usecase.GetCurrentDateUseCase
import com.example.recyc.domain.usecase.GetCurrentDayUseCase
import com.example.recyc.domain.usecase.GetCurrentRecyclerDayUseCase
import com.example.recyc.domain.usecase.PreferenceUseCase
import com.example.recyc.domain.usecase.UpdateWidgetUseCase
import com.example.recyc.utils.Logger
import com.example.recyc.utils.isOneHourBefore
import com.example.recyc.utils.showNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
internal class DailyReadWorkerTask @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workParams: WorkerParameters,
    private val getCurrentRecyclerDayUseCase: GetCurrentRecyclerDayUseCase,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
    private val preferenceUseCase: PreferenceUseCase,
    private val currentDayUseCase: GetCurrentDayUseCase,
    private val updateWidgetUseCase: UpdateWidgetUseCase,
) : CoroutineWorker(context, workParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        Logger.log("DAILY_WORKER", "doWork: Started")
        if (!preferenceUseCase.isServiceUp()) {
            Logger.log("DAILY_WORKER", "doWork: LocationService is not running, restarting it")
            val serviceIntent = Intent(context, LocationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            }
        }
        val currentModel = getCurrentRecyclerDayUseCase()
        val currentDate = getCurrentDateUseCase()
        val preferenceDate = preferenceUseCase.getLastNotificationDate()
        val isCurrentDayDone = preferenceUseCase.isCurrentDayDone(currentDayUseCase())

        val lastDayDone = preferenceUseCase.getLastDayDone()
        if (lastDayDone != currentDayUseCase()) {
            preferenceUseCase.clearConfirmationDay()
        }
        if (isOneHourBefore(currentModel?.hour) && (currentDate != preferenceDate) && !isCurrentDayDone) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Logger.log("DAILY_WORKER", "doWork: Showing notification")
                currentModel?.let { context.showNotification(it) }
                preferenceUseCase.setLastNotificationDate(currentDate.orEmpty())
            }
        }

        return try {
            updateWidgetUseCase()
            Logger.log("DAILY_WORKER", "doWork: Success")
            Result.success()
        } catch (e: Exception) {
            Log.e("DAILY_WORKER", "doWork: Error", e)
            Result.retry()
        }
    }
}