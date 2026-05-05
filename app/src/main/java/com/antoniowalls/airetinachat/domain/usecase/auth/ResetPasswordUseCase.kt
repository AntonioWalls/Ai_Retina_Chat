package com.antoniowalls.airetinachat.domain.usecase.auth

import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.domain.repository.IAuthRepository

class ResetPasswordUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(email: String): Resource<Unit> {
        return repository.resetPassword(email)
    }
}