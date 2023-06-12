package com.example.recyc.domain

import com.example.recyc.domain.model.RecyclingDayModel

interface RecyclerRepository {

    suspend fun getRecyclerDays(): List<RecyclingDayModel>
}
