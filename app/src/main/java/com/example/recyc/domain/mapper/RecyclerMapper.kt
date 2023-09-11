package com.example.recyc.domain.mapper

import com.example.recyc.R
import com.example.recyc.data.model.RecyclingResponse
import com.example.recyc.data.model.RecyclingType
import com.example.recyc.domain.model.RecyclingDayModel

fun RecyclingResponse.toModel() =
    days.map {
        RecyclingDayModel(
            day = it.day,
            hour = it.hour,
            id = it.id,
            type = it.type
        )
    }

fun RecyclingType.toIcon() = when (this) {
    RecyclingType.PLASTIC -> R.drawable.plastic_ic
    RecyclingType.GLASS -> R.drawable.glass_ic
    RecyclingType.PAPER -> R.drawable.paper_ic
    RecyclingType.WASTE -> R.drawable.waste_ic
    RecyclingType.ORGANIC -> R.drawable.wet_ic
    else -> R.drawable.ic_launcher_foreground
}
