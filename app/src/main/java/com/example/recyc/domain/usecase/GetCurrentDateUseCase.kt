package com.example.recyc.domain.usecase

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GetCurrentDateUseCase @Inject constructor() {
    operator fun invoke(): String? {
        val dayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Date().time
        val currentDate = dayFormat.format(calendar)
        return currentDate
    }
}