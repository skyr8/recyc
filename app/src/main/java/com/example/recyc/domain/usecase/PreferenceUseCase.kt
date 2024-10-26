package com.example.recyc.domain.usecase

import com.example.recyc.data.model.DayEnum
import com.example.recyc.domain.model.RecyclingDayModel

interface PreferenceUseCase {

    fun getRecyclerDay(dayId: Int): RecyclingDayModel?
    fun setRecyclerDay(dayId: Int, recyclingDayModel: RecyclingDayModel)
    fun getLastNotificationDate(): String
    fun setLastNotificationDate(date: String)
    fun isCurrentDayDone(currentDay:DayEnum?): Boolean
    fun getLastDayDone(): DayEnum
    suspend fun setDayConfirmation(date: String)
    suspend fun clearConfirmationDay()
    fun skipDay(currentDay:DayEnum?)
    fun isDaySkipped(currentDay:DayEnum?): Boolean
}