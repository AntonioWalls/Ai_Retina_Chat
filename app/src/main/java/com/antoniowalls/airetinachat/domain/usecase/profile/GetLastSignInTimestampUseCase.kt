package com.antoniowalls.airetinachat.domain.usecase.profile

import com.antoniowalls.airetinachat.domain.repository.IAuthRepository

class GetLastSignInTimestampUseCase(private val repository: IAuthRepository) {
    operator fun invoke(): Long {
        return repository.getLastSignInTimestamp()
    }
}