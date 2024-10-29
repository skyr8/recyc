package com.example.recyc.domain.usecase

import android.content.SharedPreferences
import com.example.recyc.data.model.DayEnum
import com.example.recyc.domain.model.RecyclingDayModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import javax.inject.Inject


class PreferenceUseCaseImpl @Inject constructor(private val preferences: SharedPreferences) :
    PreferenceUseCase {
    companion object {
        const val LAST_NOTIFICATION_DATE = "last_notification_date"
        const val DAY_CONFIRMATION = "day_confirmation"
        const val DAY_SKIPPED = "day_skipped"
        const val HOME_LOCATION = "home_location"
        const val SERVICE_UP = "service_up"
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
        preferences.getString(LAST_NOTIFICATION_DATE, "").orEmpty()


    override fun setLastNotificationDate(date: String) {
        preferences.edit().putString(LAST_NOTIFICATION_DATE, date).apply()
    }

    override fun isCurrentDayDone(currentDay: DayEnum?): Boolean {
        val currentDayPref = preferences.getString(DAY_CONFIRMATION, "")
        return currentDayPref == currentDay?.name
    }

    override fun getLastDayDone(): DayEnum? {
        val currentDayPref = preferences.getString(DAY_CONFIRMATION, "")
        return if(currentDayPref != "") DayEnum.valueOf(currentDayPref.orEmpty()) else null
    }

    override suspend fun setDayConfirmation(date: String) {
        preferences.edit().putString(DAY_CONFIRMATION, date).apply()
    }

    override suspend fun clearConfirmationDay() {
        preferences.edit().remove(DAY_CONFIRMATION).apply()
    }

    override fun skipDay(currentDay: DayEnum?) {
        preferences.edit().putString(DAY_SKIPPED, currentDay?.name).apply()
    }

    override fun isDaySkipped(currentDay: DayEnum?): Boolean {
        val currentDayPref = preferences.getString(DAY_SKIPPED, "")
        return currentDayPref == currentDay?.name
    }

    override fun setHomeLocation(location: LatLng) {
        val pairLocation = Pair(location.latitude, location.longitude)
        preferences.edit().putString(HOME_LOCATION, Gson().toJson(pairLocation)).apply()
    }

    override fun getHomeLocation(): LatLng? {
        val location = preferences.getString(HOME_LOCATION, null)
        location ?: return null
        val pairLocation = Gson().fromJson(location, Pair::class.java)
        return LatLng(pairLocation.first as Double, pairLocation.second as Double)
    }

    override fun isServiceUp(): Boolean {
        return preferences.getBoolean(SERVICE_UP, false)
    }

    override fun setServiceUp(isUp: Boolean) {
        preferences.edit().putBoolean(SERVICE_UP, isUp).apply()
    }
}
