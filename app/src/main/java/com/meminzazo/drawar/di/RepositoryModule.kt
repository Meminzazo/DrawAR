package com.meminzazo.drawar.di

import com.meminzazo.drawar.data.repository.OverlayRepositoryImpl
import com.meminzazo.drawar.domain.repository.OverlayRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOverlayRepository(
        impl: OverlayRepositoryImpl
    ): OverlayRepository
}