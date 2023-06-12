package com.example.recyc.domain

import com.example.recyc.data.model.RecyclingResponse
import retrofit2.http.GET

interface RecyclerClient {

    @GET("getRecyclerDays.php")
    suspend fun getRecyclerDays(): RecyclingResponse
}
