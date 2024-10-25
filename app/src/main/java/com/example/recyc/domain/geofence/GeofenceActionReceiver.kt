package com.example.recyc.domain.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.recyc.domain.usecase.PreferenceUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

// Create a BroadcastReceiver to handle the "Yes" action click
@AndroidEntryPoint
class GeofenceActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var preferenceUseCase: PreferenceUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.recyc.ACTION_CONFIRM_DAY") {
            val date = intent.getStringExtra("date") ?: return
            val notificationId = intent.getIntExtra("notificationId", -1)
            CoroutineScope(Dispatchers.IO).launch {
                preferenceUseCase.setDayConfirmation(date)
            }

            if (notificationId != -1) {
                try {
                    NotificationManagerCompat.from(context).cancel(notificationId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}