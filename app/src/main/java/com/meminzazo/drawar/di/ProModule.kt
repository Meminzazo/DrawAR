package com.meminzazo.drawar.di

import com.meminzazo.drawar.data.repository.SheetDetectionRepositoryImpl
import com.meminzazo.drawar.domain.repository.SheetDetectionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt exclusivo del Modo Pro.
 * Le dice a Hilt qué implementación concreta usar cuando
 * alguien solicite un [SheetDetectionRepository].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProModule {

    /**
     * Vincula la interfaz [SheetDetectionRepository] con su implementación
     * [SheetDetectionRepositoryImpl]. Hilt inyecta las dependencias del Impl
     * (SheetDetector y PerspectiveTransformer) automáticamente porque
     * ambas tienen @Singleton + @Inject constructor.
     */
    @Binds
    @Singleton
    abstract fun bindSheetDetectionRepository(
        impl: SheetDetectionRepositoryImpl
    ): SheetDetectionRepository
}
