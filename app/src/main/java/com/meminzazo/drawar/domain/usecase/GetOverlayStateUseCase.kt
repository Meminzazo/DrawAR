package com.meminzazo.drawar.domain.usecase

import com.meminzazo.drawar.domain.model.OverlayState
import com.meminzazo.drawar.domain.repository.OverlayRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOverlayStateUseCase @Inject constructor(
    private val repository: OverlayRepository
) {
    operator fun invoke(): Flow<OverlayState> = repository.getOverlayState()
}