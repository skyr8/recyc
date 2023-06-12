package com.example.recyc.domain.usecase

import android.icu.util.Calendar
import com.example.recyc.data.model.DayEnum
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class GetCurrentDayUseCase @Inject constructor() {
    operator fun invoke(): DayEnum? {
        val dayFormat = SimpleDateFormat("EEEE", Locale.US)
        val calendar = Calendar.getInstance()
        val weekDay = dayFormat.format(calendar.time).uppercase()
        return DayEnum.values().find { it.name == weekDay }
    }
}