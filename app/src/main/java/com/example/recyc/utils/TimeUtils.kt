package com.example.recyc.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

fun isOneHourBefore(recyclingHour: String?): Boolean {
    return isHoursBefore(recyclingHour, 1)
}

fun isHoursBefore(recyclingHour: String?, hours:Int): Boolean {
    if (recyclingHour.isNullOrEmpty()) return false

    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val recyclingTime = sdf.parse(recyclingHour) ?: return false

    val calendar = Calendar.getInstance()
    val currentTime = calendar.timeInMillis

    val recyclingCalendar = Calendar.getInstance()
    recyclingCalendar.set(Calendar.HOUR_OF_DAY, recyclingTime.hours)
    recyclingCalendar.set(Calendar.MINUTE, recyclingTime.minutes)
    recyclingCalendar.set(Calendar.SECOND, 0)
    recyclingCalendar.set(Calendar.MILLISECOND, 0)

    val recyclingTimeInMillis = recyclingCalendar.timeInMillis
    val timeDifference = recyclingTimeInMillis - currentTime
    val minutesDifference = TimeUnit.MILLISECONDS.toMinutes(timeDifference)

    val result = minutesDifference in 0..(60*hours)
    Log.d("DAILY_WORKER", "isOneHourBefore: Result = $result")
    return result
}

