package com.meminzazo.drawar.data.repository

import com.meminzazo.drawar.data.datasource.OverlayPreferencesDataSource
import com.meminzazo.drawar.domain.model.OverlayState
import com.meminzazo.drawar.domain.repository.OverlayRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OverlayRepositoryImpl @Inject constructor(
    private val dataSource: OverlayPreferencesDataSource
) : OverlayRepository {

    override fun getOverlayState(): Flow<OverlayState> {
        return dataSource.overlayState
    }

    override suspend fun saveOverlayState(state: OverlayState) {
        dataSource.save(state)
    }

    override suspend fun resetOverlayState() {
        dataSource.reset()
    }
}
