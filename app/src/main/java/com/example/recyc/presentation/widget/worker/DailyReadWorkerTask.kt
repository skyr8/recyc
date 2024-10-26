package com.example.recyc.presentation.widget.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.recyc.domain.usecase.GetCurrentDateUseCase
import com.example.recyc.domain.usecase.GetCurrentDayUseCase
import com.example.recyc.domain.usecase.GetCurrentRecyclerDayUseCase
import com.example.recyc.domain.usecase.PreferenceUseCase
import com.example.recyc.presentation.widget.updateWidget
import com.example.recyc.utils.isOneHourBefore
import com.example.recyc.utils.showNotification
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
internal class DailyReadWorkerTask @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workParams: WorkerParameters,
    private val getCurrentRecyclerDayUseCase: GetCurrentRecyclerDayUseCase,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
    private val preferenceUseCase: PreferenceUseCase,
    private val currentDayUseCase: GetCurrentDayUseCase
) : CoroutineWorker(context, workParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        Log.d("DAILY_WORKER", "doWork: Started")
        val currentModel = getCurrentRecyclerDayUseCase()
        val currentDate = getCurrentDateUseCase()
        val preferenceDate = preferenceUseCase.getLastNotificationDate()
        val model = currentModel?.copy(isDone = preferenceUseCase.isCurrentDayDone(currentDayUseCase()))
        val recyclerJson = Gson().toJson(model)

        val lastDayDone = preferenceUseCase.getLastDayDone()
        if (lastDayDone != currentDayUseCase()) {
            preferenceUseCase.clearConfirmationDay()
        }
        if (isOneHourBefore(currentModel?.hour) && (currentDate != preferenceDate)) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("DAILY_WORKER", "doWork: Showing notification")
                currentModel?.let { context.showNotification(it) }
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
}