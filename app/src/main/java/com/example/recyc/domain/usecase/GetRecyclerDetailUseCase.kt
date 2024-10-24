package com.example.recyc.domain.usecase

import com.example.recyc.domain.RecyclerRepository
import com.example.recyc.domain.model.RecyclingDayModel
import javax.inject.Inject

class GetRecyclerDetailUseCase @Inject constructor(private val recycerRepository: RecyclerRepository) {

    suspend operator fun invoke(id:Int): RecyclingDayModel? {
        return recycerRepository.getRecyclerDays().find { it.id ==id }
    }
}
