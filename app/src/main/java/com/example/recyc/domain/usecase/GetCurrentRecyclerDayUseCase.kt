package com.example.recyc.domain.usecase

import javax.inject.Inject

class GetCurrentRecyclerDayUseCase @Inject constructor(
    private val getRecyclerUseCase: GetRecyclerUseCase,
    private val currentDayUseCase: GetCurrentDayUseCase,
    ) {
    suspend operator fun invoke() = getRecyclerUseCase().find { it.day == currentDayUseCase() }
}