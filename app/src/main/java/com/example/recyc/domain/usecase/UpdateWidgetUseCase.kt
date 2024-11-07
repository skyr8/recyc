package com.example.recyc.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UpdateWidgetUseCase @Inject constructor(
    private val preferenceUseCase: PreferenceUseCase,
    private val getCurrentRecyclerDayUseCase: GetCurrentRecyclerDayUseCase,
    private val currentDayUseCase: GetCurrentDayUseCase,
    @ApplicationContext private val context: Context
) {

    suspend operator fun invoke() {
        val currentModel = getCurrentRecyclerDayUseCase()
        val model = currentModel?.copy(isDone = preferenceUseCase.isCurrentDayDone(currentDayUseCase()), isSkipped = preferenceUseCase.isDaySkipped(currentDayUseCase()))
        val recyclerJson = model?.let { com.google.gson.Gson().toJson(it) }
        recyclerJson?.let { com.example.recyc.presentation.widget.updateWidget(it, context) }
    }
}