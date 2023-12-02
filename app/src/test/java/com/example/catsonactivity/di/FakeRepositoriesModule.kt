package com.example.catsonactivity.di

import com.example.catsonactivity.model.CatsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

class FakeRepositoriesModule {

    @Provides
    @Singleton
    fun providesCatsRepository(): CatsRepository{
        return mockk()
    }
}