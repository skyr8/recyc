package com.example.recyc.data.repository

import com.example.recyc.domain.RecyclerClient
import com.example.recyc.domain.RecyclerRepository
import com.example.recyc.domain.mapper.toModel
import com.example.recyc.domain.model.RecyclingDayModel
import javax.inject.Inject

class RecyclerRepositoryImpl
@Inject constructor(private val recyclerClient: RecyclerClient) :
    RecyclerRepository {

    override suspend fun getRecyclerDays(): List<RecyclingDayModel> =
        recyclerClient.getRecyclerDays().toModel()
}
