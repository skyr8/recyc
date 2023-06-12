package com.example.recyc.data.model

data class RecyclingResponse(
    val days: List<RecyclingDay>
)

data class RecyclingDay(
    val id: Int,
    val day: DayEnum,
    val type: List<RecyclingType>,
    val hour: String
)

enum class DayEnum {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    SATURDAY,
    SUNDAY
}

enum class RecyclingType {
    PLASTIC,
    GLASS,
    PAPER,
    WASTE,
    WET
}


