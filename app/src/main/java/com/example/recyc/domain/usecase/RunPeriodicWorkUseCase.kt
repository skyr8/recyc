package com.example.recyc.domain.usecase

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
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
//    operator fun invoke() = startWorkerImmediately()

    private fun enqueueWorker() {
        val workManager = WorkManager.getInstance(activityContext)
        workManager.enqueueUniquePeriodicWork(
            "daily_read_worker_tag",
            ExistingPeriodicWorkPolicy.KEEP,
            buildRequest()
        )
    }

    private fun buildRequest(): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<DailyReadWorkerTask>(15, TimeUnit.MINUTES)
            .addTag("daily_read_worker_tag")
            .setConstraints(
                Constraints.Builder()
                    .build()
            )
            .build()
    }

    //debug function
    private fun startWorkerImmediately() {
        val workManager = WorkManager.getInstance(activityContext)
        val oneTimeWorkRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<DailyReadWorkerTask>()
            .addTag("daily_read_worker_tag")
            .setConstraints(
                Constraints.Builder()
                    .build()
            )
            .build()
        workManager.enqueue(oneTimeWorkRequest)
    }

}