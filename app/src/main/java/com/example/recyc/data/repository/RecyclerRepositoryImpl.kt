package com.example.recyc.data.repository

import com.example.recyc.data.model.DayEnum
import com.example.recyc.data.model.RecyclingType
import com.example.recyc.domain.RecyclerClient
import com.example.recyc.domain.RecyclerRepository
import com.example.recyc.domain.model.RecyclingDayModel
import com.example.recyc.domain.usecase.PreferenceUseCase
import javax.inject.Inject

class RecyclerRepositoryImpl
@Inject constructor(
    private val recyclerClient: RecyclerClient,
    private val preferencesUseCase: PreferenceUseCase
) :
    RecyclerRepository {

    override suspend fun getRecyclerDays(): List<RecyclingDayModel> {
        val list = listOf(
            preferencesUseCase.getRecyclerDay(1) ?:
            RecyclingDayModel(
                1,
                DayEnum.MONDAY,
                listOf(RecyclingType.ORGANIC),
                "22:00"
            ),
            preferencesUseCase.getRecyclerDay(2) ?:
            RecyclingDayModel(
                2,
                DayEnum.TUESDAY,
                listOf(RecyclingType.WASTE),
                "22:00"
            ),
            preferencesUseCase.getRecyclerDay(3) ?:
            RecyclingDayModel(
                3,
                DayEnum.WEDNESDAY,
                listOf(RecyclingType.PLASTIC),
                "22:00"
            ),
            preferencesUseCase.getRecyclerDay(4) ?:
            RecyclingDayModel(
                4,
                DayEnum.THURSDAY,
                listOf(RecyclingType.ORGANIC),
                "22:00"
            ),
            preferencesUseCase.getRecyclerDay(5) ?:
            RecyclingDayModel(
                5,
                DayEnum.FRIDAY,
                listOf(RecyclingType.PAPER),
                "22:00"
            ),
            preferencesUseCase.getRecyclerDay(6) ?:
            RecyclingDayModel(
                6,
                DayEnum.SATURDAY,
                listOf(RecyclingType.ORGANIC),
                "22:00"
            ),
            preferencesUseCase.getRecyclerDay(7) ?:
            RecyclingDayModel(
                7,
                DayEnum.SUNDAY,
                listOf(RecyclingType.NONE),
                "22:00"
            )
        )
        return list
    }
}
