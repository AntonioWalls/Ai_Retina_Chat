package com.antoniowalls.airetinachat.domain.usecase.auth

import com.antoniowalls.airetinachat.domain.repository.IAuthRepository

class LogoutUseCase(private val repository: IAuthRepository) {
    operator fun invoke() {
        repository.logout()
    }
}