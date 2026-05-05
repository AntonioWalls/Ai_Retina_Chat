package com.antoniowalls.airetinachat.domain.usecase.profile

import com.antoniowalls.airetinachat.data.model.Resource
import com.antoniowalls.airetinachat.domain.repository.IAuthRepository

class ChangePasswordUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(newPass: String): Resource<Unit> {
        return repository.changePassword(newPass)
    }
}