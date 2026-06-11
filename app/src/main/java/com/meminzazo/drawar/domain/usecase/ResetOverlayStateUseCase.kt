package com.meminzazo.drawar.domain.usecase

import com.meminzazo.drawar.domain.repository.OverlayRepository
import javax.inject.Inject

class ResetOverlayStateUseCase @Inject constructor(
    private val repository: OverlayRepository
) {
    suspend operator fun invoke() = repository.resetOverlayState()
}