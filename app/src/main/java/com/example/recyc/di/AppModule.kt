package com.example.recyc.di

import android.content.Context
import android.content.SharedPreferences
import com.example.recyc.data.repository.RecyclerRepositoryImpl
import com.example.recyc.domain.RecyclerClient
import com.example.recyc.domain.RecyclerRepository
import com.example.recyc.domain.usecase.PreferenceUseCase
import com.example.recyc.domain.usecase.PreferenceUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

        @Provides
        @Singleton
        fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
            return context.getSharedPreferences("recycler_preferences", Context.MODE_PRIVATE)
        }
    }

    @Singleton
    @Binds
    abstract fun RecyclerRepositoryImpl.bindRecyclerRepository():
        RecyclerRepository

    @Singleton
    @Binds
    abstract fun PreferenceUseCaseImpl.bindPreference():
            PreferenceUseCase

}