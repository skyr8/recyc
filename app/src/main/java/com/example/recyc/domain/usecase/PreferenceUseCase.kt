package com.example.recyc.domain.usecase

import com.example.recyc.data.model.DayEnum
import com.example.recyc.domain.model.RecyclingDayModel

interface PreferenceUseCase {

    fun getRecyclerDay(dayId: Int): RecyclingDayModel?
    fun setRecyclerDay(dayId: Int, recyclingDayModel: RecyclingDayModel)
}