package com.antoniowalls.airetinachat.domain.usecase.profile

import com.antoniowalls.airetinachat.domain.repository.IAuthRepository

class GetExtraProfileDataUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(): Map<String, Any>? {
        return repository.getExtraProfileData()
    }
}