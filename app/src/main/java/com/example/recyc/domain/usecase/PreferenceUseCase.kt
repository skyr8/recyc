package com.example.recyc.domain.usecase

import com.example.recyc.data.model.DayEnum
import com.example.recyc.domain.model.RecyclingDayModel
import com.google.android.gms.maps.model.LatLng

interface PreferenceUseCase {

    fun getRecyclerDay(dayId: Int): RecyclingDayModel?
    fun setRecyclerDay(dayId: Int, recyclingDayModel: RecyclingDayModel)
    fun getLastNotificationDate(): String
    fun setLastNotificationDate(date: String)
    fun isCurrentDayDone(currentDay:DayEnum?): Boolean
    fun getLastDayDone(): DayEnum?
    suspend fun setDayConfirmation(date: String)
    suspend fun clearConfirmationDay()
    fun skipDay(currentDay:DayEnum?, isSkipped:Boolean = true)
    fun isDaySkipped(currentDay:DayEnum?): Boolean
    fun setHomeLocation(location: LatLng)
    fun getHomeLocation(): LatLng?
    fun isServiceUp(): Boolean
    fun setServiceUp(isUp: Boolean)
}