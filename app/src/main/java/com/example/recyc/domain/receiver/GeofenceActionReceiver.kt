package com.example.recyc.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.recyc.domain.usecase.GetCurrentDayUseCase
import com.example.recyc.domain.usecase.GetCurrentRecyclerDayUseCase
import com.example.recyc.domain.usecase.PreferenceUseCase
import com.example.recyc.presentation.widget.updateWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CONFIRM_DAY = "com.example.recyc.ACTION_CONFIRM_DAY"
        const val ACTION_DISMISS_DAY = "com.example.recyc.ACTION_DISMISS_DAY"
        const val ACTION_CONFIRM_DAY_GLOBAL = "com.example.recyc.ACTION_CONFIRM_DAY_GLOBAL"
        const val NOTIFICATION_ID = "notificationId"
        const val DATE = "date"
        const val IS_CONFIRMED = "isConfirmed"
    }

    @Inject
    lateinit var preferenceUseCase: PreferenceUseCase

    @Inject
    lateinit var currentDayUseCase: GetCurrentDayUseCase

    @Inject
    lateinit var getCurrentRecyclerDayUseCase: GetCurrentRecyclerDayUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CONFIRM_DAY) {
            val date = intent.getStringExtra(DATE) ?: return
            val notificationId = intent.getIntExtra(NOTIFICATION_ID, -1)
            CoroutineScope(Dispatchers.IO).launch {
                preferenceUseCase.setDayConfirmation(date)
                updateWidget(context)
            }


            val globalIntent = Intent(ACTION_CONFIRM_DAY_GLOBAL).apply {
                putExtra(IS_CONFIRMED, true)
            }
            context.sendBroadcast(globalIntent)

            cancelNotification(notificationId, context)
        } else if (intent.action == ACTION_DISMISS_DAY) {
            val notificationId = intent.getIntExtra(NOTIFICATION_ID, -1)
            CoroutineScope(Dispatchers.IO).launch {
                preferenceUseCase.skipDay(currentDayUseCase())
            }

            cancelNotification(notificationId, context)
        }
    }

    private fun cancelNotification(notificationId: Int, context: Context) {
        if (notificationId != -1) {
            try {
                NotificationManagerCompat.from(context).cancel(notificationId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun updateWidget(context: Context) {
        val currentModel = getCurrentRecyclerDayUseCase()
        val model = currentModel?.copy(isDone = preferenceUseCase.isCurrentDayDone(currentDayUseCase()))
        val recyclerJson = model?.let { com.google.gson.Gson().toJson(it) }
        recyclerJson?.let { updateWidget(it, context) }
    }
}
