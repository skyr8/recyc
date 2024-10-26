package com.example.recyc.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.recyc.domain.usecase.GetCurrentDayUseCase
import com.example.recyc.domain.usecase.PreferenceUseCase
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
    }

    @Inject
    lateinit var preferenceUseCase: PreferenceUseCase

    @Inject
    lateinit var currentDayUseCase: GetCurrentDayUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CONFIRM_DAY) {
            val date = intent.getStringExtra("date") ?: return
            val notificationId = intent.getIntExtra("notificationId", -1)
            CoroutineScope(Dispatchers.IO).launch {
                preferenceUseCase.setDayConfirmation(date)
            }

            val globalIntent = Intent(ACTION_CONFIRM_DAY_GLOBAL).apply {
                putExtra("isConfirmed", true)
            }
            context.sendBroadcast(globalIntent)

            if (notificationId != -1) {
                try {
                    NotificationManagerCompat.from(context).cancel(notificationId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (intent.action == ACTION_DISMISS_DAY) {
            CoroutineScope(Dispatchers.IO).launch {
                preferenceUseCase.skipDay(currentDayUseCase())
            }
        }
    }
}
