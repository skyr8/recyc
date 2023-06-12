package com.example.recyc.presentation.widget

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll

suspend fun updateWidget(recyclingDayModel: String, context: Context) {
    // Iterate through all the available glance id's.
    GlanceAppWidgetManager(context).getGlanceIds(AppWidget::class.java).forEach { glanceId ->
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[stringPreferencesKey("recycle_data_key")] = recyclingDayModel //new value
        }
    }
    AppWidget().updateAll(context)
}