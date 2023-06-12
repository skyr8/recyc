package com.example.recyc.presentation.widget.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.recyc.domain.usecase.GetCurrentDayUseCase
import com.example.recyc.domain.usecase.GetRecyclerUseCase
import com.example.recyc.presentation.widget.updateWidget
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
internal class DailyReadWorkerTask @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workParams: WorkerParameters,
    private val getRecyclerUseCase: GetRecyclerUseCase,
    private val currentDayUseCase: GetCurrentDayUseCase
) : CoroutineWorker(context, workParams) {

    override suspend fun doWork(): Result {
        val currentModel = getRecyclerUseCase().find { it.day == currentDayUseCase() }
        val recyclerJson = Gson().toJson(currentModel)
        return try {
            updateWidget(recyclerJson, context)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}