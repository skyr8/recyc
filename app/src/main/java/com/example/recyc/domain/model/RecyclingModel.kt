package com.example.recyc.domain.model

import com.example.recyc.data.model.DayEnum
import com.example.recyc.data.model.RecyclingType
import java.io.Serializable

data class RecyclingDayModel(
    val id: Int,
    val day: DayEnum,
    val type: List<RecyclingType>,
    val hour: String,
    val isDone: Boolean = false,
):Serializable



