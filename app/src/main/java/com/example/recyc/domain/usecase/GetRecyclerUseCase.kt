package com.example.recyc.domain.usecase

import com.example.recyc.domain.RecyclerRepository
import com.example.recyc.domain.model.RecyclingDayModel
import javax.inject.Inject

class GetRecyclerUseCase @Inject constructor(private val recycerRepository: RecyclerRepository) {

    suspend operator fun invoke(): List<RecyclingDayModel> {
        return recycerRepository.getRecyclerDays()
    }
}
