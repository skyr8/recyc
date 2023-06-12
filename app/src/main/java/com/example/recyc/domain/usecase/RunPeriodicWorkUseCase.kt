package com.example.recyc.domain.usecase

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.recyc.presentation.widget.worker.DailyReadWorkerTask
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ActivityScoped
class RunPeriodicWorkUseCase @Inject constructor(
    @ActivityContext private val activityContext: Context
) {

    operator fun invoke() = enqueueWorker()

    private fun enqueueWorker() {
        val workManager = WorkManager.getInstance(activityContext)
        workManager.enqueueUniquePeriodicWork(
            "daily_read_worker_tag",
            ExistingPeriodicWorkPolicy.KEEP,
            buildRequest()
        )
    }

    private fun buildRequest(): PeriodicWorkRequest {
        // 1 day
        return PeriodicWorkRequestBuilder<DailyReadWorkerTask>(1, TimeUnit.HOURS)
            .addTag("daily_read_worker_tag")
            .setConstraints(
                Constraints.Builder()
                    // Network required
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
    }
}