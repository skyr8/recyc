package com.example.recyc.domain.usecase

import android.content.SharedPreferences
import com.example.recyc.data.model.DayEnum
import com.example.recyc.domain.model.RecyclingDayModel
import com.google.gson.Gson
import javax.inject.Inject


class PreferenceUseCaseImpl @Inject constructor(private val preferences: SharedPreferences) :
    PreferenceUseCase {
        companion object{
            const val LAST_NOTIFICATION_DATE = "last_notification_date"
            const val DAY_CONFIRMATION = "day_confirmation"
        }
    override fun getRecyclerDay(dayId: Int): RecyclingDayModel? {
        preferences.getString(dayId.toString(), null)?.let {
            return Gson().fromJson(it, RecyclingDayModel::class.java)
        } ?: run {
            return null
        }
    }

    override fun setRecyclerDay(dayId: Int, recyclingDayModel: RecyclingDayModel) {
        preferences.edit().putString(dayId.toString(), Gson().toJson(recyclingDayModel)).apply()
    }

    override fun getLastNotificationDate(): String =
        preferences.getString(LAST_NOTIFICATION_DATE,"").orEmpty()


    override fun setLastNotificationDate(date: String) {
        preferences.edit().putString(LAST_NOTIFICATION_DATE, date).apply()
    }

    override fun getDayConfirmation(currentDay: DayEnum): Boolean {
        val currentDayPref = preferences.getString(DAY_CONFIRMATION, "")
        return currentDayPref == currentDay.name
    }

    override suspend fun setDayConfirmation(date: String) {
        DayEnum.values().forEach {
            preferences.edit().remove(it.name).apply()
        }
        preferences.edit().putString(DAY_CONFIRMATION, date).apply()
    }

}