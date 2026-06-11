package com.meminzazo.drawar.domain.repository

import com.meminzazo.drawar.domain.model.OverlayState
import kotlinx.coroutines.flow.Flow

interface OverlayRepository {
    fun getOverlayState(): Flow<OverlayState>
    suspend fun saveOverlayState(state: OverlayState)
    suspend fun resetOverlayState()
}