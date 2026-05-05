package com.antoniowalls.airetinachat.domain.usecase.profile

import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.domain.repository.IAuthRepository

class Update2FAStateUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(isEnabled: Boolean): Resource<Unit> {
        return repository.update2FAState(isEnabled)
    }
}