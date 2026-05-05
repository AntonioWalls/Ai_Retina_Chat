package com.antoniowalls.airetinachat.domain.usecase.profile

import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.domain.repository.IAuthRepository

class ReAuthenticateAndChangePasswordUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(currentPass: String, newPass: String): Resource<Unit> {
        return repository.reAuthenticateAndChangePassword(currentPass, newPass)
    }
}