package com.example.recyc.di

import com.example.recyc.data.repository.RecyclerRepositoryImpl
import com.example.recyc.domain.RecyclerClient
import com.example.recyc.domain.RecyclerRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    companion object {
        private val BASE_URL = "https://13536.000webhostapp.com/recycler/"

        @Singleton
        @Provides
        fun provideLoginClient(retrofit: Retrofit): RecyclerClient {
            return retrofit.create(RecyclerClient::class.java)
        }

        @Provides
        @Singleton
        fun providesRetrofit(): Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Binds
    abstract fun RecyclerRepositoryImpl.bindRecyclerRepository():
        RecyclerRepository

}