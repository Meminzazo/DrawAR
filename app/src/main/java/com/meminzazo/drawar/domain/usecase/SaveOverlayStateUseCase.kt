package com.meminzazo.drawar.domain.usecase

import com.meminzazo.drawar.domain.model.OverlayState
import com.meminzazo.drawar.domain.repository.OverlayRepository
import jakarta.inject.Inject

class SaveOverlayStateUseCase @Inject constructor(
    private val repository: OverlayRepository
) {
    suspend operator fun invoke(state: OverlayState) = repository.saveOverlayState(state)
}